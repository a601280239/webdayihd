package com.manger.controller;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.hqf.pojo.ResponseResult;
import com.hqf.pojo.entity.RoleInfo;
import com.hqf.pojo.entity.UserInfo;
import com.hqf.pojo.entity.UserRoleInfo;
import com.hqf.utils.MD5;
import com.hqf.utils.TwitterIdWorker;
import com.manger.dao.RoleInfoDao;
import com.manger.dao.UserInfoDao;
import com.manger.dao.UserRoleDao;
import com.manger.service.UserService;
import com.manger.util.MailUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName UserController
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/7
 **/
@RestController
@Api(value = "用户相关业务",tags = "用户操作相关")
public class UserController {
    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private ThumbImageConfig thumbImageConfig;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    UserService userService;
    @Autowired
    UserInfoDao userInfoDao;
    @Value("${deleteUrl}")
    private String deleteUrl;
    @ApiOperation(value = "查询用户列表", notes = "分页模糊查询用户列表")
    @RequestMapping("/selectYh")
    public  Object selectUser(String name,String dt1, String dt2,
                              Integer sex,@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "5") Integer pageSize){

        return userService.testQuery(name,dt1,dt2,sex,page,pageSize);
    }
    @ApiOperation(value = "删除用户", notes = "删除用户")
    @RequestMapping("deleteYh")
    public Object deleteYh(Long id){
        return userService.deleteById(id);
    }
    @ApiOperation(value = "上传修改用户信息", notes = "上传修改用户信息")
    @RequestMapping("updateUser")
    @Transactional
    public ResponseResult updateUser(@RequestParam("file") MultipartFile file,Long id,String userName,String loginName,String tel,Integer sex,String uemail) throws IOException {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserInfo> byLoginName = userInfoDao.findByLoginName(loginName);
        if(byLoginName!=null&&byLoginName.size()>0&&!id.toString().equals(byLoginName.get(0).getId().toString())){
            responseResult.setCode(500);
            return responseResult;
        }

        List<UserInfo> byTel = userInfoDao.findByTel(tel);

        if(byTel!=null&&byTel.size()>0&&!id.toString().equals(byTel.get(0).getId().toString())){
            responseResult.setCode(415);
            return responseResult;
        }
        List<UserInfo> byUemail = userInfoDao.findByUemail(uemail);
        if(byUemail!=null&&byUemail.size()>0&&!id.toString().equals(byUemail.get(0).getId().toString())){
            responseResult.setCode(416);
            return responseResult;
        }
        if(!byLoginName.get(0).getUrl().equals("M00/00/00/wKjhhF1TYV-AaVQ-AADqjljJCsY667")&&!byLoginName.get(0).getUrl().equals("M00/00/00/wKjhhF1TZjeAZ_x8AARtG_cCip4511")){
            storageClient.deleteFile(deleteUrl+byLoginName.get(0).getUrl()+"_100x100.png");
            storageClient.deleteFile(deleteUrl+byLoginName.get(0).getUrl()+".png");
        }

        String path = file.getOriginalFilename();
        File file1 = new File("E:/img/"+path);
        // 上传并且生成缩略图
        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(
                new FileInputStream(file1), file1.length(), "png", null);
        String path1 = thumbImageConfig.getThumbImagePath(storePath.getPath());



        UserInfo userInfo=userInfoDao.findById(id).get();
        userInfo.setUserName(userName);
        userInfo.setLoginName(loginName);
        userInfo.setSex(sex);
        userInfo.setTel(tel);
        userInfo.setUrl( path1.substring(0,path1.lastIndexOf("_")));
        userInfo.setUemail(uemail);
        userInfoDao.save(userInfo);
        if(id.toString().equals(byLoginName.get(0).getId().toString())){
            UserInfo userByLogin = userService.getUserByLogin(loginName);
            responseResult.setResult(userByLogin);
        }


        responseResult.setCode(200);
        return responseResult;
    }
    @ApiOperation(value = "上传添加用户信息", notes = "上传添加用户信息")
    @RequestMapping("addUser")
    public ResponseResult addUser(@RequestParam("file") MultipartFile multipartFile,String userName,String loginName,String passWord,String tel,Integer sex,String uemail) throws IOException {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserInfo> byLoginName = userInfoDao.findByLoginName(loginName);
        if(byLoginName!=null&&byLoginName.size()>0){
            responseResult.setCode(500);
            return responseResult;
        }
        List<UserInfo> byTel = userInfoDao.findByTel(tel);
        if(byTel!=null&&byTel.size()>0){
            responseResult.setCode(415);
            return responseResult;
        }
        List<UserInfo> byUemail = userInfoDao.findByUemail(uemail);
        if(byUemail!=null&&byUemail.size()>0){
            responseResult.setCode(416);
            return responseResult;
        }
        TwitterIdWorker twitterIdWorker=new TwitterIdWorker();
        long next = twitterIdWorker.nextId();
        String path = multipartFile.getOriginalFilename();
        File file = new File("E:/img/"+path);
        // 上传并且生成缩略图
        StorePath storePath = this.storageClient.uploadImageAndCrtThumbImage(
                new FileInputStream(file), file.length(), "png", null);
        String path1 = thumbImageConfig.getThumbImagePath(storePath.getPath());


        UserInfo userInfo=new UserInfo();
        userInfo.setUserName(userName);

        userInfo.setId(next);
        userInfo.setLoginName(loginName);
        userInfo.setSex(sex);
        userInfo.setTel(tel);
        userInfo.setPassword(MD5.encryptPassword(passWord,"hqf"));
        userInfo.setUrl(path1.substring(0,path1.lastIndexOf("_")));
        userInfo.setUemail(uemail);
        userInfoDao.save(userInfo);
        UserRoleInfo userRoleInfo =new UserRoleInfo();
        userRoleInfo.setId(twitterIdWorker.nextId());
        userRoleInfo.setUserId(userInfo.getId());
        userRoleInfo.setRoleId(1160875187323920384l);

        userRoleDao.save(userRoleInfo);

        responseResult.setCode(200);
        return responseResult;
    }
    @ApiOperation(value = "增加用户", notes = "增加用户")

    @RequestMapping("/addYh")
    public ResponseResult addYh(@RequestBody UserInfo userInfo) throws FileNotFoundException {
        userInfo.setPassword(MD5.encryptPassword(userInfo.getPassword(), "hqf"));
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserInfo> byLoginName = userInfoDao.findByLoginName(userInfo.getLoginName());
        if (byLoginName!=null&&byLoginName.size() > 0) {
            responseResult.setCode(500);
            return responseResult;
        }

        List<UserInfo> tel = userInfoDao.findByTel(userInfo.getTel());
        if(tel!=null&&tel.size()>0){
            responseResult.setCode(415);
            return responseResult;
        }
        List<UserInfo> byUemail = userInfoDao.findByUemail(userInfo.getUemail());
        if(byUemail!=null&&byUemail.size()>0){
            responseResult.setCode(416);
            return responseResult;
        }
        if(userInfo.getSex()==1){


            userInfo.setUrl("M00/00/00/wKjhhF1TYV-AaVQ-AADqjljJCsY667");
        }else{

            userInfo.setUrl("M00/00/00/wKjhhF1TZjeAZ_x8AARtG_cCip4511");
        }
        TwitterIdWorker twitterIdWorker=new TwitterIdWorker();
        userInfo.setId(twitterIdWorker.nextId());
        userInfoDao.save(userInfo);
        UserRoleInfo userRoleInfo =new UserRoleInfo();
        userRoleInfo.setId(twitterIdWorker.nextId());
        userRoleInfo.setUserId(userInfo.getId());
        userRoleInfo.setRoleId(1160875187323920384l);
        userRoleDao.save(userRoleInfo);

        responseResult.setCode(200);
        return responseResult;
    }
    @ApiOperation(value = "修改用户", notes = "修改用户")
    @Transactional
    @RequestMapping("/updateYh")
    public ResponseResult updateYh(@RequestBody UserInfo userInfo){
        System.out.println("userInfo = [" + userInfo + "]");
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserInfo> byLoginName = userInfoDao.findByLoginName(userInfo.getLoginName());


        if(byLoginName!=null&&byLoginName.size()>0&&!userInfo.getId().toString().equals(byLoginName.get(0).getId().toString())){

                responseResult.setCode(500);
                return responseResult;


        }
        List<UserInfo> tel = userInfoDao.findByTel(userInfo.getTel());
        if(tel!=null&&tel.size()>0&&!userInfo.getId().toString().equals(tel.get(0).getId().toString())){
            responseResult.setCode(415);
            return responseResult;
        }
        List<UserInfo> byUemail = userInfoDao.findByUemail(userInfo.getUemail());
        if(byUemail!=null&&byUemail.size()>0&&!userInfo.getId().toString().equals(byUemail.get(0).getId().toString())){
            responseResult.setCode(416);
            return responseResult;
        }
        userInfoDao.save(userInfo);
        if(userInfo.getId().toString().equals(byLoginName.get(0).getId().toString())){
            UserInfo userByLogin = userService.getUserByLogin(userInfo.getLoginName());
            responseResult.setResult(userByLogin);
        }
        responseResult.setCode(200);
        return responseResult;
    }
    @Autowired
    private RoleInfoDao roleInfoDao;
    @ApiOperation(value = "查询角色", notes = "根据所登录用户的等级查询角色")

    @RequestMapping("/findRole")
    public List<RoleInfo> findRole(@RequestBody Map<String,String> map){

        return  roleInfoDao.selectRoleInfo(Integer.parseInt(map.get("leval")));
    }
    @Autowired
    private UserRoleDao userRoleDao;
    @ApiOperation(value = "绑定角色", notes = "先解绑角色再绑定角色")

    @RequestMapping("/addUr")
    @Transactional
    public ResponseResult addUr(@RequestBody Map<String,Object> map){
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserRoleInfo> userId = userRoleDao.findByUserId(Long.parseLong(map.get("userId").toString()));
        if(userId!=null&&userId.size()>0){
            userRoleDao.deleteByUserId(Long.parseLong(map.get("userId").toString()));
        }
        UserRoleInfo userRoleInfo=new UserRoleInfo();
        TwitterIdWorker twitterIdWorker=new TwitterIdWorker();
        long id = twitterIdWorker.nextId();
        userRoleInfo.setId(id);
        userRoleInfo.setUserId(Long.parseLong(map.get("userId").toString()));
        userRoleInfo.setRoleId(Long.parseLong(map.get("roleId").toString()));
        userRoleDao.save(userRoleInfo);

        responseResult.setCode(200);
        return responseResult;
    }
    @ApiOperation(value = "下载数据", notes = "下载当前页数据")
    @RequestMapping("/downexcel")
    public ResponseResult downExcel(String name,String dt1, String dt2,
                                    Integer sex,@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "5") Integer pageSize) throws IOException {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserInfo> userInfos = userService.testQuery1(name, dt1, dt2, sex, page, pageSize);
        XSSFWorkbook workbook = new XSSFWorkbook();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = "用户信息表"+simpleDateFormat.format(new Date())+".xlsx";
        String sheetName = "用户信息";
        String[] titile ={"编号","用户名","登录名","性别","密码"};
        XSSFSheet sheet = workbook.createSheet(sheetName);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        XSSFRow titleRow = sheet.createRow(0);
        for (int i=0 ; i<titile.length ; i++) {
            //创建单元格
            XSSFCell cell = titleRow.createCell(i);
            //设置单元格内容
            cell.setCellValue(titile[i]);

            //设置单元格样式
            cell.setCellStyle(style);
        }
        responseResult.setCode(200);
        Row row = null;
        for(int i = 0; i < userInfos.size();i++){
            //创建list.siza()行数据
            row = sheet.createRow(i + 1);
            //把值写进单元格
            row.createCell(0).setCellValue(userInfos.get(i).getId());
            row.createCell(1).setCellValue(userInfos.get(i).getUserName());
            row.createCell(2).setCellValue(userInfos.get(i).getLoginName());
            row.createCell(3).setCellValue(userInfos.get(i).getSex());
            row.createCell(4).setCellValue(userInfos.get(i).getPassword());
        }
        File file = new File("D:/biao");
        if(!file.exists()){
            file.mkdirs();
        }
        String savePath = "D:/biao/"+fileName;
        FileOutputStream fileOut = new FileOutputStream(savePath);
        workbook.write(fileOut);
        fileOut.close();
        return responseResult;
    }
    @ApiOperation(value = "上传添加用户", notes = "上传添加用户")
    /*@ApiImplicitParam(
            name = "multipartFile",
            required = true,
            dataType = "multipartFile",
            dataTypeClass = MultipartFile.class
    )*/
    @Transactional
    @RequestMapping("dowloudExcel")
    @ResponseBody
    public ResponseResult dowloudExcel(@RequestParam("file") MultipartFile multipartFile ) throws IOException {
        ResponseResult responseResult=ResponseResult.getResponseResult();
        //获取传来的excel的输入流
        InputStream inputStream = multipartFile.getInputStream();

        List<UserInfo> userList = new ArrayList<>();
        //获得一个XSSFWorkbook的对象
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        //确认数据在哪个工作空间
        XSSFSheet sheet = workbook.getSheetAt(0);
        //
        String str="";
        int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();
        TwitterIdWorker twitterIdWorker=new TwitterIdWorker();
        for (int i = 1; i < physicalNumberOfRows; i++) {
            XSSFRow row = sheet.getRow(i);
            UserInfo user = new UserInfo();
            user.setId(twitterIdWorker.nextId());
            user.setUserName(row.getCell(1).getStringCellValue());
            List<UserInfo> byLoginName = userInfoDao.findByLoginName(row.getCell(2).getStringCellValue());
            if(byLoginName!=null&&byLoginName.size()>0){
                str+=byLoginName.get(0).getUserName()+",";
                continue;
            }
            user.setLoginName(row.getCell(2).getStringCellValue());
           if(new Double(row.getCell(3).getNumericCellValue()).intValue()!=1&&new Double(row.getCell(3).getNumericCellValue()).intValue()!=2){
               str+=row.getCell(1).getStringCellValue()+",";
               continue;
           }
            user.setSex(new Double(row.getCell(3).getNumericCellValue()).intValue());
           String reg = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
            Pattern compile = Pattern.compile(reg);
            Matcher matcher = compile.matcher("" + new Double(row.getCell(4).getNumericCellValue()).longValue());
            if(!matcher.matches()){
                str+=row.getCell(1).getStringCellValue()+",";
                continue;
            }
            List<UserInfo> byTel = userInfoDao.findByTel("" + new Double(row.getCell(4).getNumericCellValue()).longValue());
            if(byTel!=null&&byTel.size()>0){
                str+=byTel.get(0).getUserName()+",";
                continue;
            }
            user.setTel(""+new Double(row.getCell(4).getNumericCellValue()).longValue());


            user.setPassword(MD5.encryptPassword("123456","hqf"));
            if(user.getSex()==1){
                user.setUrl("M00/00/00/wKjhhF1TYV-AaVQ-AADqjljJCsY667");
            }else{
                user.setUrl("M00/00/00/wKjhhF1TZjeAZ_x8AARtG_cCip4511");
            }
            userInfoDao.save(user);
            UserRoleInfo userRoleInfo =new UserRoleInfo();
            userRoleInfo.setId(twitterIdWorker.nextId());
            userRoleInfo.setUserId(user.getId());
            userRoleInfo.setRoleId(1160875187323920384l);
            userRoleDao.save(userRoleInfo);


        }
        if(str.equals("")){
            responseResult.setCode(200);
        }else{
            responseResult.setCode(413);
            responseResult.setError(str+"这些数据无法添加");
        }


        return responseResult;
    }
    @Value("${spring.mail.username}")
    private String from;
    @Autowired
    private JavaMailSender mailSender;
    @RequestMapping("/findUemail")
    public ResponseResult getUemail(@RequestBody Map<String,Object> map) throws MessagingException {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        List<UserInfo> byUemail = userInfoDao.findByUemail(map.get("uemail").toString());
        if(byUemail!=null&&byUemail.size()>0){
            new Thread(new MailUtil(map.get("uemail").toString(), byUemail.get(0).getId().toString())).start();
            redisTemplate.opsForValue().set("email"+byUemail.get(0).getId(),"");
            redisTemplate.expire("email"+byUemail.get(0).getId(),5, TimeUnit.MINUTES);
            responseResult.setCode(200);
        }else{
            responseResult.setCode(500);

        }
        return responseResult;
    }
    @RequestMapping("/findId")
    public ResponseResult findId(@RequestBody Map<String,Object> map) {
        ResponseResult responseResult = ResponseResult.getResponseResult();
        if(!redisTemplate.hasKey("email"+map.get("id"))){
            responseResult.setCode(500);
            return responseResult;
        }
        responseResult.setCode(200);
        return responseResult;
    }
    @Transactional
    @RequestMapping("/xgMm")
    public ResponseResult xgMm(@RequestBody Map<String,Object> map) {
        ResponseResult responseResult = ResponseResult.getResponseResult();
       if(redisTemplate.hasKey("email"+map.get("id"))){
           UserInfo userInfo = userInfoDao.findById(Long.parseLong(map.get("id").toString())).get();
           userInfo.setPassword(MD5.encryptPassword(map.get("password").toString(),"hqf"));
           userInfoDao.save(userInfo);

           responseResult.setCode(200);
           return responseResult;
       }else{
           responseResult.setCode(500);
           return responseResult;
       }

    }
}