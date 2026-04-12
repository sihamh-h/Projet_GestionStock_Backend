package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.model.dto.RegisterUserRequestDTO;
import fr.doranco.GestionStock.model.dto.UserCreateDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;
import fr.doranco.GestionStock.model.entity.User;


import java.util.List;

public interface IUserService {
       List<User> getUsers() throws Exception;
       User getUser(long id) throws Exception;
       User getUser(String email) throws Exception;
       void addOrUpdateUser(User user) throws Exception;
       void deleteUser(Long id) throws Exception;
       List<User> getUsersByRoleId(long roleId) throws Exception;
       String getRoleByUserId(long userId) throws Exception;
       List<String> getPermissionsByUserId(long userId) throws Exception;
       int countUsersByRoleId(long roleId) throws Exception;
       void createAdmin(User admin) throws Exception;
       UserResponseDTO createUser(UserCreateDTO registerUserDTO) throws Exception;




}
