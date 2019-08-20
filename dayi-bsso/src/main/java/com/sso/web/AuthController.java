package com.sso.web;

import com.alibaba.fastjson.JSON;
import com.hqf.exception.LoginException;
import com.hqf.jwt.JWTUtils;
import com.hqf.pojo.ResponseResult;
import com.hqf.pojo.entity.UserInfo;
import com.hqf.randm.VerifyCodeUtils;
import com.hqf.utils.HttpUtils;
import com.hqf.utils.MD5;
import com.hqf.utils.UID;
import com.sso.dao.UserDao;
import com.sso.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javafx.fxml.LoadException;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName AuthControllrt
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/5
 **/
@Controller
@Api(value = "登录业务" ,tags = "登录业务")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @ApiOperation(value = "获取验证码", notes = "获取验证码")
    @RequestMapping("/getCode")
   @ResponseBody
    public ResponseResult getCode(HttpServletRequest request, HttpServletResponse response){
      String code = VerifyCodeUtils.generateVerifyCode(5);
      ResponseResult responseResult=ResponseResult.getResponseResult();
      responseResult.setResult(code);
      String uidCode="CODE"+ UID.getUUID16();
      redisTemplate.opsForValue().set(uidCode,code);
      redisTemplate.expire(uidCode,1, TimeUnit.MINUTES);
      Cookie cookie = new Cookie("authcode",uidCode);
      cookie.setPath("/");
      cookie.setDomain("localhost");
      response.addCookie(cookie);
      return responseResult;
  }
  @ApiOperation(value = "登录功能", notes = "登录功能")
  @ApiImplicitParams({
          @ApiImplicitParam(
                  name = "loginname",
                  required = true,
                  dataType = "string",
                  dataTypeClass = String.class
          ),
          @ApiImplicitParam(
                  name = "sex",
                  required = true,
                  dataType = "string",
                  dataTypeClass = String.class
          ),
          @ApiImplicitParam(
                  name = "password",
                  required = true,
                  dataType = "string",
                  dataTypeClass = String.class
          ),

          @ApiImplicitParam(
                  name = "code",
                  required = true,
                  dataType = "string",
                  dataTypeClass = String.class
          ),
          @ApiImplicitParam(
                  name = "codekey",
                  required = true,
                  dataType = "string",
                  dataTypeClass = String.class
          )
  })
  @ResponseBody
  @RequestMapping("/login")
    public ResponseResult toLogin(@RequestBody Map<String,Object> map) throws LoadException, LoginException {
      ResponseResult responseResult=ResponseResult.getResponseResult();
      //获取生成的验证码
      String code = redisTemplate.opsForValue().get(map.get("codekey").toString());
      //获取传入的验证码是否是生成后存在redis中的验证码
      if(code==null||!code.equals(map.get("code").toString()) ){
          responseResult.setCode(500);
          responseResult.setError("验证码错误,请重新刷新页面登陆");
          return responseResult;
      }
      //进行用户密码的校验
      if(map!=null&&map.get("loginname")!=null){
          //根据用户名获取用户信息
          UserInfo user = userService.getUserByLogin(map.get("loginname").toString());
          if(user!=null){
              //比对密码
              String password = MD5.encryptPassword(map.get("password").toString(), "hqf");
              if(user.getPassword().equals(password)){

                  //将用户信息转存为JSON串
                  String userinfo = JSON.toJSONString(user);

                  //将用户信息使用JWt进行加密，将加密信息作为票据
                  String token = JWTUtils.generateToken(userinfo);

                  //将加密信息存入statuInfo
                  responseResult.setToken(token);
                  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
                  Date date = new Date();
                  String format = simpleDateFormat.format(date);
                  if(!redisTemplate.hasKey("tj"+user.getId())){
                      redisTemplate.opsForValue().set("tj"+user.getId(),format);
                      redisTemplate.expire(user.getId().toString(),1440,TimeUnit.MINUTES);

                      redisTemplate.opsForHash().increment("number",format,1);

                      if(redisTemplate.opsForHash().size("number")>7){
                          Set<Object> number = redisTemplate.opsForHash().keys("number");
                          for (Object o : number) {
                              redisTemplate.opsForHash().delete("number",o.toString());
                              break;
                          }

                      }

                  }else{
                      String s = redisTemplate.opsForValue().get("tj" + user.getId());
                      if(!s.equals(format)){
                          redisTemplate.delete("tj"+user.getId());
                          redisTemplate.opsForValue().set("tj"+user.getId(),format);
                          redisTemplate.expire(user.getId().toString(),1440,TimeUnit.MINUTES);

                          redisTemplate.opsForHash().increment("number",format,1);

                          if(redisTemplate.opsForHash().size("number")>7){
                              Set<Object> number = redisTemplate.opsForHash().keys("number");
                              for (Object o : number) {
                                  redisTemplate.opsForHash().delete("number",o.toString());
                                  break;
                              }
                          }
                      }
                  }
 redisTemplate.opsForValue().set("USERINFO"+user.getId().toString(),token);
                  //将该用户的数据访问权限信息存入缓存中
                redisTemplate.delete("USERDATAAUTH"+user.getId().toString());
                  //将该用户的数据访问权限信息存入缓存中
                  redisTemplate.opsForHash().putAll("USERDATAAUTH"+user.getId().toString(),user.getAuthmap());
                  //设置token过期 30分钟
                  redisTemplate.expire("USERINFO"+user.getId().toString(),600,TimeUnit.SECONDS);
                  //设置返回值user
                  responseResult.setResult(user);
                  responseResult.setCode(200);
                  //设置成功信息
                  responseResult.setSuccess("登陆成功！^_^");

                  return responseResult;
              }else{
                  throw new LoginException("用户名或密码错误");
              }
          }else{
              throw new LoginException("用户名或密码错误");
          }
      }else{
          throw new LoginException("用户名或密码错误");
      }
  }
  @ApiOperation(value = "退出功能", notes = "退出功能")
  @RequestMapping("/loginout")
  @ResponseBody
   public ResponseResult  loginout(@RequestBody Map<String,Object> map){
      ResponseResult responseResult=ResponseResult.getResponseResult();
      redisTemplate.delete("USERDATAAUTH"+map.get("id").toString());
      redisTemplate.delete("USERINFO"+map.get("id").toString());

      responseResult.setSuccess("ok");
      return responseResult;
  }
    @ApiOperation(value = "最近七天登录次数", notes = "最近七天登录次数")
  @RequestMapping("loginCount")
  @ResponseBody
    public Object loginCount(){
      Map<String, List> map =new HashMap<>();
      Cursor<Map.Entry<Object, Object>> number = redisTemplate.opsForHash().scan("number", ScanOptions.NONE);
      List<String> key1=new ArrayList<>();
      List<String> value1=new ArrayList<>();
      while(number.hasNext()){
          Map.Entry<Object, Object> entry = number.next();
          key1.add(entry.getKey().toString());
          value1.add(entry.getValue().toString());
      }
      map.put("key1",key1);
      map.put("value1",value1);
      return map;
    }
    @RequestMapping("/phoneGetcode")
    @ResponseBody
    public ResponseResult phoneGetcode(@RequestBody Map<String,Object> map){


        ResponseResult responseResult = ResponseResult.getResponseResult();
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";

        String appcode = "8ca8562169a84f46af2b528389f7f244";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile",map.get("phone").toString() );
        //验证码
      Integer code=  (int)(Math.random()*9000)+1000;
      redisTemplate.opsForValue().set(map.get("phone").toString(),code.toString());
        redisTemplate.expire(map.get("phone").toString(),1, TimeUnit.MINUTES);
        querys.put("param", "code:"+code);
        querys.put("tpl_id", "TP1711063");
        Map<String, String> bodys = new HashMap<String, String>();


        try {

            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            responseResult.setCode(200);
            return  responseResult;
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
            responseResult.setCode(500);
            return  responseResult;

        }





    }
    @ResponseBody
    @RequestMapping("/loginPhone")
    public ResponseResult loginPhone(@RequestBody Map<String,Object> map) throws LoadException, LoginException {
        ResponseResult responseResult=ResponseResult.getResponseResult();
        //获取生成的验证码
        String code = redisTemplate.opsForValue().get(map.get("phone").toString());
        //获取传入的验证码是否是生成后存在redis中的验证码
        if(code==null||!code.equals(map.get("code").toString()) ){
            responseResult.setCode(500);
            responseResult.setError("验证码错误,请重新输入");
            return responseResult;
        }
        //进行用户密码的校验
        if(map!=null&&map.get("phone")!=null){

            UserInfo user =userService.getUserTel(map.get("phone").toString());
            if(user!=null){
                    //将用户信息转存为JSON串
                    String userinfo = JSON.toJSONString(user);
                    //将用户信息使用JWt进行加密，将加密信息作为票据
                    String token = JWTUtils.generateToken(userinfo);
                    //将加密信息存入statuInfo
                    responseResult.setToken(token);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
                    Date date = new Date();
                    String format = simpleDateFormat.format(date);
                    if(!redisTemplate.hasKey("tj"+user.getId())){
                        redisTemplate.opsForValue().set("tj"+user.getId(),format);
                        redisTemplate.expire(user.getId().toString(),1440,TimeUnit.MINUTES);
                        //将生成的token存储到redis库
                        redisTemplate.opsForHash().increment("number",format,1);

                        if(redisTemplate.opsForHash().size("number")>7){
                            Set<Object> number = redisTemplate.opsForHash().keys("number");
                            for (Object o : number) {
                                redisTemplate.opsForHash().delete("number",o.toString());
                                break;
                            }

                        }

                    }else{
                        String s = redisTemplate.opsForValue().get("tj" + user.getId());
                        if(!s.equals(format)){
                            redisTemplate.delete("tj"+user.getId());
                            redisTemplate.opsForValue().set("tj"+user.getId(),format);
                            redisTemplate.expire(user.getId().toString(),1440,TimeUnit.MINUTES);

                            redisTemplate.opsForHash().increment("number",format,1);

                            if(redisTemplate.opsForHash().size("number")>7){
                                Set<Object> number = redisTemplate.opsForHash().keys("number");
                                for (Object o : number) {
                                    redisTemplate.opsForHash().delete("number",o.toString());
                                    break;
                                }
                            }
                        }
                    }
                    redisTemplate.opsForValue().set("USERINFO"+user.getId().toString(),token);
                    //将该用户的数据访问权限信息存入缓存中
                    redisTemplate.delete("USERDATAAUTH"+user.getId().toString());
                    //将该用户的数据访问权限信息存入缓存中
                    redisTemplate.opsForHash().putAll("USERDATAAUTH"+user.getId().toString(),user.getAuthmap());
                    //设置token过期 30分钟
                    redisTemplate.expire("USERINFO"+user.getId().toString(),600,TimeUnit.SECONDS);
                    //设置返回值user
                    responseResult.setResult(user);
                    responseResult.setCode(200);
                    //设置成功信息
                    responseResult.setSuccess("登陆成功！^_^");

                    return responseResult;

            }else{
                throw new LoginException("用户名或密码错误");
            }
        }else{
            throw new LoginException("用户名或密码错误");
        }
    }
}