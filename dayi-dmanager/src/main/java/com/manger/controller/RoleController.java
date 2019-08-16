package com.manger.controller;

import com.hqf.pojo.ResponseResult;
import com.hqf.pojo.entity.*;
import com.hqf.utils.TwitterIdWorker;
import com.manger.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName RoleController
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/9
 **/
@RestController
public class RoleController {
    @Autowired
    private LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    UserInfoDao userInfoDao;
    @Autowired
    private RoleMenuDao roleMenuDao;
    @Autowired
    private RoleInfoDao roleInfoDao;
    @Autowired
    private MenuInfoDao menuInfoDao;
    @RequestMapping("selectRole")
    public Object selectRole(@RequestBody Map<String,Object> map){

        EntityManager entityManager = localContainerEntityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager();
        entityManager.clear();
        StringBuffer stringBuffer = new StringBuffer("select * from base_role where 1=1");

        StringBuffer stringBufferCount=new StringBuffer("select count(bu.id) as totalCount from base_role bu where 1=1");

        if(!map.get("name").toString().equals("")&&map.get("name")!=null){

            stringBuffer.append(" and roleName like concat('%','"+map.get("name").toString()+"','%')");
            stringBufferCount.append(" and roleName like concat('%','"+map.get("name").toString()+"','%')");
        }


        stringBuffer.append(" limit "+(Integer.parseInt(map.get("page").toString())-1)*Integer.parseInt(map.get("pageSize").toString())+","+Integer.parseInt(map.get("pageSize").toString()));

        //查列表
        Query nativeQuery = entityManager.createNativeQuery(stringBuffer.toString(), RoleInfo.class);
        List<RoleInfo> resultList = nativeQuery.getResultList();
        if(resultList!=null&&resultList.size()>0){
            for (RoleInfo roleInfo : resultList) {
                List<UserInfo> userInfos = userInfoDao.forUserInfoByUserId(roleInfo.getId());
               roleInfo.setUserInfos(userInfos);
               roleInfo.setMenuInfoList(menuInfoDao.getRoleMenuInfo(roleInfo.getId()));


            }
        }




        //总条数
        Query nativeQueryCount = entityManager.createNativeQuery(stringBufferCount.toString());

        Map<String,Object> map1=new HashMap<String,Object>();

        map1.put("content",resultList);

        map1.put("totalElements",nativeQueryCount.getSingleResult());

        return  map1;
    }


    @RequestMapping("/delRole")
    @Transactional
    public ResponseResult delRole(Long id){
        ResponseResult responseResult=new ResponseResult();
        try {
            List<UserRoleInfo> byRoleId = userRoleDao.findByRoleId(id);
            if(byRoleId!=null&&byRoleId.size()>0){
                userRoleDao.deleteAll(byRoleId);
            }
            List<RoleMenuInfo> byRoleId1 = roleMenuDao.findByRoleId(id);
            if(byRoleId1!=null&&byRoleId1.size()>0){
               roleMenuDao.deleteAll(byRoleId1);
            }
            roleInfoDao.deleteById(id);
            responseResult.setCode(200);
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setCode(500);
        }
        return responseResult;
    }

    @RequestMapping("/addRole")
    public ResponseResult addRole(@RequestBody RoleInfo roleInfo) {

        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<RoleInfo> byRoleName = roleInfoDao.findByRoleName(roleInfo.getRoleName());
        if (byRoleName!=null&&byRoleName.size() > 0) {
            responseResult.setCode(500);
            return responseResult;
        }
        TwitterIdWorker twitterIdWorker=new TwitterIdWorker();
        long next = twitterIdWorker.nextId();
        roleInfo.setId(next);
        roleInfoDao.save(roleInfo);
        return responseResult;
    }
    @RequestMapping("/findMenu")
    public List<MenuInfo> findMenu(){
        return getForMenuInfo(0l);
    }
    public List<MenuInfo> getForMenuInfo(Long mid){
        List<MenuInfo> firstMenuInfo = menuInfoDao.findByParentId(mid);
        if(firstMenuInfo!=null){
            for (MenuInfo menuInfo : firstMenuInfo) {
                menuInfo.setMenuInfoList(getForMenuInfo(menuInfo.getId()));
            }
        }
            return firstMenuInfo;
    }



    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping("/addRm")
    @Transactional
    public ResponseResult addRm(@RequestBody RoleInfo roleInfo){
        ResponseResult responseResult=new ResponseResult();
        roleInfoDao.save(roleInfo);
        Long urid = roleInfo.getUrid();
        List<MenuInfo> roleMenuInfo = menuInfoDao.getRoleMenuInfo(urid);
        Long ids1[] = new Long[roleMenuInfo.size()];

        List<MenuInfo> roleMenuInfo1 = menuInfoDao.getRoleMenuInfo(roleInfo.getId());

       if(roleMenuInfo1!=null&&roleMenuInfo1.size()>roleMenuInfo.size()){
    roleMenuInfo1.retainAll(roleMenuInfo);

           for (MenuInfo menuInfo : roleMenuInfo1) {
               String sql1="delete from base_role_menu where roleId=? and menuId=?";
               jdbcTemplate.update(sql1,new Object[]{roleInfo.getId(),menuInfo.getId()});
               
           }
       }else{
               String sql1="delete from base_role_menu where roleId=?";
               jdbcTemplate.update(sql1,roleInfo.getId());
       }
        String[] ids = roleInfo.getIds();
        if(ids!=null&&!ids.equals("")) {
            String sql="insert into base_role_menu(id,roleId,menuId) values(?,?,?)";
            List<Object[]>list=new ArrayList<>();
            TwitterIdWorker twitterIdWorker = new TwitterIdWorker();
            for (String s : ids) {

                list.add(new Object[]{twitterIdWorker.nextId(),roleInfo.getId(),Long.parseLong(s)});
            }
            jdbcTemplate.batchUpdate(sql,list);
        }

        responseResult.setCode(200);


        return responseResult;
    }

}