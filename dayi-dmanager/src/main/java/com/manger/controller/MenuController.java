package com.manger.controller;

import com.hqf.pojo.ResponseResult;
import com.hqf.pojo.entity.MenuInfo;
import com.hqf.utils.TwitterIdWorker;
import com.manger.dao.MenuInfoDao;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(value = "权限操作相关",tags = "权限操作相关")
public class MenuController {
    @Autowired
    private MenuInfoDao menuInfoDao;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @ApiOperation(value = "查询权限", notes = "递归查询权限")
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
    @ApiOperation(value = "添加权限", notes = "添加权限，赋给一级角色")

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
    @ApiOperation(value = "修改权限名称", notes = "修改权限名称")

    @RequestMapping("/updateMenu")
    public ResponseResult updateMenu(@RequestBody MenuInfo menuInfo){
        ResponseResult responseResult=ResponseResult.getResponseResult();
        menuInfoDao.save(menuInfo);
        menuInfoDao.flush();
        responseResult.setCode(200);
        return responseResult;
    }
    @ApiOperation(value = "删除权限", notes = "删除权限，并删除该权限相关一切数据")
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