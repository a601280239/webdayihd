package com.manger.service;

import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.hqf.pojo.entity.RoleInfo;
import com.hqf.pojo.entity.UserInfo;
import com.manger.dao.RoleInfoDao;
import com.manger.dao.UserInfoDao;
import com.manger.dao.UserRoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserService
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/7
 **/
@Component
public class UserService {
    @Autowired
    RoleInfoDao roleInfoDao;
    @Autowired
    UserInfoDao userInfoDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean;
    @Autowired
    private FastFileStorageClient storageClient;
    @Value("${deleteUrl}")
    private String deleteUrl;
    public Object testQuery(String name,String dt1,String dt2,Integer sex,Integer page,Integer pageSize){
                EntityManager entityManager = localContainerEntityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager();
        entityManager.clear();
        StringBuffer stringBuffer = new StringBuffer("select * from base_user where 1=1");


        StringBuffer stringBufferCount=new StringBuffer("select count(bu.id) as totalCount from base_user bu where 1=1");

        if(!name.equals("")&&name!=null){

            stringBuffer.append(" and userName like concat('%','"+name+"','%')");
            stringBufferCount.append(" and userName like concat('%','"+name+"','%')");
        }
        if((!dt1.equals("")&&dt1!=null)&&(dt2.equals("")||dt2==null)){
            stringBuffer.append(" and createTime > '"+dt1+"' ");
            stringBufferCount.append(" and createTime > '"+dt1+"' ");
        }
        if((dt1.equals("")||dt1==null)&&(!dt2.equals("")&&dt2!=null)){
            stringBuffer.append(" and createTime < '"+dt2+"' ");
            stringBufferCount.append(" and createTime < '"+dt2+"' ");
        }

        if((!dt1.equals("")&&dt1!=null)&&(!dt2.equals("")&&dt2!=null)){
            stringBuffer.append(" and createTime between '"+dt1+"' and '"+dt2+"'");
            stringBufferCount.append(" and createTime between '"+dt1+"' and '"+dt2+"' ");
        }
        if(sex!=0){

            stringBuffer.append(" and sex="+sex);
            stringBufferCount.append(" and sex="+sex);
        }
        stringBuffer.append(" order by createTime desc");
        stringBuffer.append(" limit "+(page-1)*pageSize+","+pageSize);

        //查列表
        Query nativeQuery = entityManager.createNativeQuery(stringBuffer.toString(), UserInfo.class);


        List<UserInfo> resultList = nativeQuery.getResultList();
        if(resultList!=null&&resultList.size()>0){
            for (UserInfo userInfo : resultList) {
                RoleInfo roleInfo = roleInfoDao.forRoleInfoByUserId(userInfo.getId());
               if(roleInfo!=null){
                   userInfo.setRoleInfo(roleInfo);
                   userInfo.setRoleName(roleInfo.getRoleName());
               }
            }
        }

        //总条数
        Query nativeQueryCount = entityManager.createNativeQuery(stringBufferCount.toString());

        Map<String,Object> map=new HashMap<String,Object>();

        map.put("content",resultList);

        map.put("totalElements",nativeQueryCount.getSingleResult());

        return  map;

    }
    @Transactional
    public Object deleteById(Long id){
        Map<String,Object> map =new HashMap<String, Object>();

        try {
            UserInfo userInfo = userInfoDao.findById(id).get();
            String url = userInfo.getUrl();


            userRoleDao.deleteByUserId(id);
            System.out.println("id = [" + id + "]");

            userInfoDao.delete(userInfo);
            if(!userInfo.getUrl().equals("M00/00/00/wKjhhF1TYV-AaVQ-AADqjljJCsY667")&&!userInfo.getUrl().equals("M00/00/00/wKjhhF1TZjeAZ_x8AARtG_cCip4511")){
                storageClient.deleteFile(deleteUrl+userInfo.getUrl()+"_100x100.png");
                storageClient.deleteFile(deleteUrl+userInfo.getUrl()+".png");
            }


            map.put("code",200);

        } catch (Exception e) {
            e.printStackTrace();
            map.put("code",400);
        }

        return map;
    }
    public List<UserInfo> testQuery1(String name,String dt1,String dt2,Integer sex,Integer page,Integer pageSize){
        EntityManager entityManager = localContainerEntityManagerFactoryBean.getNativeEntityManagerFactory().createEntityManager();
        entityManager.clear();
        StringBuffer stringBuffer = new StringBuffer("select * from base_user where 1=1");




        if(!name.equals("")&&name!=null){

            stringBuffer.append(" and userName like concat('%','"+name+"','%')");

        }
        if((!dt1.equals("")&&dt1!=null)&&(dt2.equals("")||dt2==null)){
            stringBuffer.append(" and createTime > '"+dt1+"' ");

        }
        if((dt1.equals("")||dt1==null)&&(!dt2.equals("")&&dt2!=null)){
            stringBuffer.append(" and createTime < '"+dt2+"' ");

        }

        if((!dt1.equals("")&&dt1!=null)&&(!dt2.equals("")&&dt2!=null)){
            stringBuffer.append(" and createTime between '"+dt1+"' and '"+dt2+"'");

        }
        if(sex!=0){

            stringBuffer.append(" and sex="+sex);

        }
        stringBuffer.append(" order by createTime desc");
        stringBuffer.append(" limit "+(page-1)*pageSize+","+pageSize);

        //查列表
        Query nativeQuery = entityManager.createNativeQuery(stringBuffer.toString(), UserInfo.class);





        return  nativeQuery.getResultList();

    }
}