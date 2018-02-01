package com.zeros.demo.dao;

import com.zeros.demo.model.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created on 2018/1/31.
 *
 * @author 迹_Jason
 */
@Mapper
public interface RoleDao {

    @Select("SELECT * FROM t_role WHERE id = #{id}")
    Role findById(@Param("id") int id);

    @Select("SELECT * FROM t_role")
    List<Role> fetchRoles();
}
