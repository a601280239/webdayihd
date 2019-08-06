package com.sso.web;

import com.alibaba.fastjson.JSON;
import com.hqf.exception.LoginException;
import com.hqf.jwt.JWTUtils;
import com.hqf.pojo.ResponseResult;
import com.hqf.pojo.entity.UserInfo;
import com.hqf.randm.VerifyCodeUtils;
import com.hqf.utils.MD5;
import com.hqf.utils.UID;
import com.sso.service.UserService;
import javafx.fxml.LoadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName AuthControllrt
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/5
 **/
@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
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
  @ResponseBody
  @RequestMapping("login")
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

                  //将生成的token存储到redis库
                  redisTemplate.opsForValue().set("USERINFO"+user.getId().toString(),token);
                  //将该用户的数据访问权限信息存入缓存中


                  //设置token过期 30分钟
                  redisTemplate.expire("USERINFO"+user.getId().toString(),600,TimeUnit.SECONDS);
                  //设置返回值
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
}