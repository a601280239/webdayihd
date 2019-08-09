package com.manger.controller;

import com.hqf.pojo.ResponseResult;
import com.hqf.pojo.entity.RoleInfo;
import com.hqf.pojo.entity.UserInfo;
import com.hqf.pojo.entity.UserRoleInfo;
import com.hqf.utils.MD5;
import com.hqf.utils.UID;
import com.manger.dao.RoleInfoDao;
import com.manger.dao.UserInfoDao;
import com.manger.dao.UserRoleDao;
import com.manger.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserController
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/7
 **/
@RestController
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    UserInfoDao userInfoDao;
    @RequestMapping("/selectYh")
    public  Object selectUser(String name,String dt1, String dt2,
                              Integer sex,@RequestParam(defaultValue = "1") Integer page,@RequestParam() Integer pageSize){

        return userService.testQuery(name,dt1,dt2,sex,page,pageSize);
    }
    @RequestMapping("deleteYh")
    public Object deleteYh(Long id){
        return userService.deleteById(id);
    }
    @RequestMapping("updateUser")
    public ResponseResult updateUser(@RequestParam("file") MultipartFile multipartFile,Long id,String userName,String loginName,String tel,Integer sex) throws IOException {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserInfo> byLoginName = userInfoDao.findByLoginName(loginName);
        if(byLoginName.size()>0&&!id.toString().equals(byLoginName.get(0).getId().toString())){
            responseResult.setCode(500);
            return responseResult;
        }
        if(byLoginName.get(0).getUrl()!=null&&!byLoginName.equals("")){
            File file1 =new File("E:/img/"+byLoginName.get(0).getUrl());
            file1.delete();
        }
        File file =new File("E:/img/"+id+"/");
        if(!file.exists()){
            file.mkdirs();
        }


        UserInfo userInfo=userInfoDao.findById(id).get();
        userInfo.setUserName(userName);
        userInfo.setLoginName(loginName);
        userInfo.setSex(sex);
        userInfo.setTel(tel);
        userInfo.setUrl(id+"/"+multipartFile.getOriginalFilename());
        userInfoDao.save(userInfo);

        multipartFile.transferTo(new File("E:/img/"+id+"/"+multipartFile.getOriginalFilename()));
        responseResult.setCode(200);
        return responseResult;
    }
    @RequestMapping("addUser")
    public ResponseResult addUser(@RequestParam("file") MultipartFile multipartFile,String userName,String loginName,String passWord,String tel,Integer sex) throws IOException {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserInfo> byLoginName = userInfoDao.findByLoginName(loginName);
        if(byLoginName.size()>0){
            responseResult.setCode(500);
            return responseResult;
        }
        long next = UID.next();
        File file =new File("E:/img/"+next+"/");
        if(!file.exists()){
            file.mkdirs();
        }


        UserInfo userInfo=new UserInfo();
        userInfo.setUserName(userName);
        userInfo.setId(next);
        userInfo.setLoginName(loginName);
        userInfo.setSex(sex);
        userInfo.setTel(tel);
        userInfo.setPassword(MD5.encryptPassword(passWord,"hqf"));
        userInfo.setUrl(next+"/"+multipartFile.getOriginalFilename());
        userInfoDao.save(userInfo);

        multipartFile.transferTo(new File("E:/img/"+next+"/"+multipartFile.getOriginalFilename()));
        responseResult.setCode(200);
        return responseResult;
    }
    @RequestMapping("/addYh")
    public ResponseResult addYh(@RequestBody UserInfo userInfo) {
        userInfo.setPassword(MD5.encryptPassword(userInfo.getPassword(), "hqf"));
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserInfo> byLoginName = userInfoDao.findByLoginName(userInfo.getLoginName());
        if (byLoginName.size() > 0) {
            responseResult.setCode(500);
            return responseResult;
        }
        userInfo.setId(UID.next());
        userInfoDao.save(userInfo);
        responseResult.setCode(200);
        return responseResult;
    }
    @RequestMapping("/updateYh")
    public ResponseResult updateYh(@RequestBody UserInfo userInfo){
        System.out.println("userInfo = [" + userInfo + "]");
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserInfo> byLoginName = userInfoDao.findByLoginName(userInfo.getLoginName());


        if(byLoginName.size()>0&&!userInfo.getId().toString().equals(byLoginName.get(0).getId().toString())){

                responseResult.setCode(500);
                return responseResult;


        }

        userInfoDao.save(userInfo);
        responseResult.setCode(200);
        return responseResult;
    }
    @Autowired
    private RoleInfoDao roleInfoDao;
    @RequestMapping("/findRole")
    public List<RoleInfo> findRole(){
        roleInfoDao.findAll();
        return  roleInfoDao.findAll();
    }
    @Autowired
    private UserRoleDao userRoleDao;
    @RequestMapping("/addUr")
    public ResponseResult addUr(@RequestBody Map<String,Object> map){
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserRoleInfo> userId = userRoleDao.findByUserId(Long.parseLong(map.get("userId").toString()));
        System.out.println(map);
        if(userId!=null&&userId.size()>0){
            userRoleDao.delByUserId(Long.parseLong(map.get("userId").toString()));
        }
        UserRoleInfo userRoleInfo=new UserRoleInfo();
        userRoleInfo.setId(UID.next());
        userRoleInfo.setUserId(Long.parseLong(map.get("userId").toString()));
        userRoleInfo.setRoleId(Long.parseLong(map.get("roleId").toString()));
        userRoleDao.save(userRoleInfo);
        responseResult.setCode(200);
        return responseResult;
    }



}