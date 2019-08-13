package com.sso.service;

import com.hqf.pojo.entity.MenuInfo;
import com.hqf.pojo.entity.RoleInfo;
import com.hqf.pojo.entity.UserInfo;
import com.sso.dao.MenuDao;
import com.sso.dao.RoleDao;
import com.sso.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserService
 * @Description: TODO
 * @Author 黄庆丰
 * @Date 2019/8/5
 **/
@Component
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;
    @Autowired
    private MenuDao menuDao;
    public UserInfo getUserByLogin(String loginName){
        UserInfo byLoginName = userDao.findByLoginName(loginName);
        if(byLoginName!=null){
            RoleInfo roleInfo = roleDao.forRoleInfoByUserId(byLoginName.getId());
            byLoginName.setRoleInfo(roleInfo);
            Map<String,String> map =new Hashtable<>();
            System.out.println(roleInfo.getId());
            List<MenuInfo> forMenuInfo = getForMenuInfo(roleInfo.getId(), 0l, map);
            byLoginName.setListMenuInfo(forMenuInfo);
            byLoginName.setAuthmap(map);
        }
        return byLoginName;
    }
    public List<MenuInfo> getForMenuInfo(Long roleId, Long mid, Map<String,String> map){
        List<MenuInfo> firstMenuInfo = menuDao.getFirstMenuInfo(roleId, mid);
        if(firstMenuInfo!=null){
            for (MenuInfo menuInfo : firstMenuInfo) {
                if(menuInfo.getLeval()==4){
                    map.put(menuInfo.getUrl(),"");
                }
                menuInfo.setMenuInfoList(getForMenuInfo(roleId,menuInfo.getId(),map));
            }
        }
        return  firstMenuInfo;
    }

}