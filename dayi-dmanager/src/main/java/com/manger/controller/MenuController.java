package com.manger.controller;

import com.hqf.pojo.ResponseResult;
import com.hqf.pojo.entity.MenuInfo;
import com.hqf.utils.TwitterIdWorker;
import com.manger.dao.MenuInfoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName MenuController
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/10
 **/
@RestController
public class MenuController {
    @Autowired
    private MenuInfoDao menuInfoDao;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @RequestMapping("/findAllMenu")
    public List<MenuInfo> findMenu(){
        return getForMenuInfo(0l);
    }
    public List<MenuInfo> getForMenuInfo(Long mid){
        menuInfoDao.flush();
        List<MenuInfo> firstMenuInfo = menuInfoDao.findByParentId(mid);
        if(firstMenuInfo!=null){
            for (MenuInfo menuInfo : firstMenuInfo) {
                menuInfo.setMenuInfoList(getForMenuInfo(menuInfo.getId()));
            }
        }
        return firstMenuInfo;
    }
    @RequestMapping("/addMenu")
    public ResponseResult addMenu(@RequestBody MenuInfo menuInfo){

        ResponseResult responseResult=ResponseResult.getResponseResult();
        TwitterIdWorker twitterIdWorker=new TwitterIdWorker();

        menuInfo.setId(twitterIdWorker.nextId());
        menuInfoDao.save(menuInfo);
        if(menuInfo.getUleval()==1){
            String sql="insert into base_role_menu(id,roleId,menuId) values(?,?,?)";
            jdbcTemplate.update(sql,new Object[]{twitterIdWorker.nextId(),menuInfo.getUrid(),menuInfo.getId()});
        }
        menuInfoDao.flush();
        responseResult.setCode(200);
        return responseResult;
    }
    @RequestMapping("/updateMenu")
    public ResponseResult updateMenu(@RequestBody MenuInfo menuInfo){
        ResponseResult responseResult=ResponseResult.getResponseResult();
        menuInfoDao.save(menuInfo);
        menuInfoDao.flush();
        responseResult.setCode(200);
        return responseResult;
    }
    @Transactional
    @RequestMapping("/delMenu")
    public ResponseResult delMenu(@RequestBody MenuInfo menuInfo){
        ResponseResult responseResult=ResponseResult.getResponseResult();
        Long[] ids = menuInfo.getIds();
        List<MenuInfo> allById = menuInfoDao.findAllById(Arrays.asList(ids));
        menuInfoDao.deleteAll(allById);
        responseResult.setCode(200);
        return responseResult;
    }

}