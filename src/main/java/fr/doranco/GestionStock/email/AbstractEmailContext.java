package fr.doranco.GestionStock.email;

import fr.doranco.GestionStock.model.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class AbstractEmailContext implements IEmailStrategy {
    private User user;
    //private String token;
    private String frontendUrl;

    public AbstractEmailContext(User user, String frontendUrl) {
        this.user = user;
        this.frontendUrl = frontendUrl;
    }
}
