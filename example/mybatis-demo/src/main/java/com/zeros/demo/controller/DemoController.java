package com.zeros.demo.controller;

import com.ecfront.dew.common.Page;
import com.ecfront.dew.common.Resp;
import com.zeros.demo.dao.RoleDao;
import com.zeros.demo.model.Role;
import com.zeros.mybatis.plugin.MybatisPageContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created on 2018/1/31.
 *
 * @author 迹_Jason
 */
@RestController
@RequestMapping("demo")
public class DemoController {

    @Autowired
    private RoleDao roleDao;

    @GetMapping("{id}")
    public Resp<Role> getRoleById(@PathVariable("id") int id) {
        return Resp.success(roleDao.findById(id));
    }

    @GetMapping
    public Resp<Page<Role>> fetchRoles() {
        MybatisPageContext.setPageRequest(new MybatisPageContext.PageRequest(1, 2));
        roleDao.fetchRoles();
        Page<Role> roleResponsePage = MybatisPageContext.getPage();
        MybatisPageContext.clearAll();
        return Resp.success(roleResponsePage);
    }
}
