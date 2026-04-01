package fr.doranco.GestionStock.email;

import fr.doranco.GestionStock.enums.TokenType;

public interface IEmailStrategy {
    String getSubject();
    String buildContent(String name, String tokenOrPwd);
    TokenType getSupportedType();
}
