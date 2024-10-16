package br.com.kgsm;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

@OpenAPIDefinition(
        info = @Info(
                title = "Api - Quarkus Social",
                version = "2.0",
                contact = @Contact(
                        name = "Kaua Moreira",
                        url = "http://kgsm.com",
                        email = "contato.kauagsm@gmail.com"
                ),
                license = @License(
                        name = "Apache 2.0"
                )
        )
)
public class QuarkusSocialApplication extends Application {

}
