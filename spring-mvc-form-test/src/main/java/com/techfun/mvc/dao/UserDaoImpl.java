package com.techfun.mvc.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.techfun.mvc.model.User;

@Repository("userDao")
public class UserDaoImpl implements UserDao {

	@Autowired
	DataSource datasource;

	@Autowired
	private JdbcTemplate JdbcTemplate;

	public User findById(Integer id) {

		String sql = "select * from users where id=" + id;
		List<User> users = JdbcTemplate.query(sql, new UserMapper());
		return users.get(0);

	}

	public List<User> findAll() {

		String sql = "select * from users";
		List<User> users = JdbcTemplate.query(sql, new UserMapper());

		return users;

	}

	public void save(User user) {

		User userEntity = (User) getSqlParameterByModel(user);
		JdbcTemplate.update(
				"INSERT INTO users(NAME, EMAIL, ADDRESS, PASSWORD, NEWSLETTER, FRAMEWORK, SEX, NUMBER, COUNTRY, SKILL) values(?,?,?,?,?,?,?,?,?,?)",
				userEntity.getName(), userEntity.getEmail(), userEntity.getAddress(), userEntity.getPassword(),
				userEntity.isNewsletter(), userEntity.getFramework(), userEntity.getSex(), userEntity.getNumber(),
				userEntity.getCountry(), userEntity.getSkill());
	}

	public void update(User user) {

		String frameWork = convertListToDelimitedString(user.getFramework());
		String skill = convertListToDelimitedString(user.getSkill());
		JdbcTemplate.update(
				"UPDATE users SET NAME = ?, EMAIL=?, ADDRESS=?, PASSWORD=?, NEWSLETTER=?, FRAMEWORK=?, SEX=?, NUMBER=?, COUNTRY=?, SKILL=? WHERE ID=?",
				user.getName(), user.getEmail(), user.getAddress(), user.getPassword(), user.isNewsletter(), frameWork,
				user.getSex(), user.getNumber(), user.getCountry(), skill, user.getId());
	}

	public void delete(Integer id) {

	}

	private SqlParameterSource getSqlParameterByModel(User user) {

		// Unable to handle List<String> or Array
		// BeanPropertySqlParameterSource

		MapSqlParameterSource paramSource = new MapSqlParameterSource();
		paramSource.addValue("id", user.getId());
		paramSource.addValue("name", user.getName());
		paramSource.addValue("email", user.getEmail());
		paramSource.addValue("address", user.getAddress());
		paramSource.addValue("password", user.getPassword());
		paramSource.addValue("newsletter", user.isNewsletter());

		// join String
		paramSource.addValue("framework", convertListToDelimitedString(user.getFramework()));
		paramSource.addValue("sex", user.getSex());
		paramSource.addValue("number", user.getNumber());
		paramSource.addValue("country", user.getCountry());
		paramSource.addValue("skill", convertListToDelimitedString(user.getSkill()));

		return paramSource;
	}

	class UserMapper implements RowMapper<User> {

		public User mapRow(ResultSet rs, int arg1) throws SQLException {
			User user = new User();
			user.setId(rs.getInt("id"));
			user.setName(rs.getString("name"));
			user.setEmail(rs.getString("email"));
			user.setFramework(convertDelimitedStringToList(rs.getString("framework")));
			user.setAddress(rs.getString("address"));
			user.setCountry(rs.getString("country"));
			user.setNewsletter(rs.getBoolean("newsletter"));
			user.setNumber(rs.getInt("number"));
			user.setPassword(rs.getString("password"));
			user.setSex(rs.getString("sex"));
			user.setSkill(convertDelimitedStringToList(rs.getString("skill")));
			return user;
		}
	}

	private static List<String> convertDelimitedStringToList(String delimitedString) {

		List<String> result = new ArrayList<String>();

		if (!StringUtils.isEmpty(delimitedString)) {
			result = Arrays.asList(StringUtils.delimitedListToStringArray(delimitedString, ","));
		}
		return result;

	}

	private String convertListToDelimitedString(List<String> list) {

		String result = "";
		if (list != null) {
			result = StringUtils.arrayToCommaDelimitedString(list.toArray());
		}
		return result;

	}

}