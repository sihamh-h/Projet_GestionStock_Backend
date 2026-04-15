package fr.doranco.GestionStock.service;


import fr.doranco.GestionStock.model.dto.UpdateUserDTO;
import fr.doranco.GestionStock.model.dto.UserCreateDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;



import java.util.List;

public interface IUserService {
       List<UserResponseDTO> getUsers() throws Exception;
       UserResponseDTO getUserById(Long id) throws Exception;
       UserResponseDTO  updateUser(Long id, UpdateUserDTO updateUserDto) throws Exception;
       void deleteUser(Long id) throws Exception;
       UserResponseDTO createUser(UserCreateDTO registerUserDTO) throws Exception;




}
