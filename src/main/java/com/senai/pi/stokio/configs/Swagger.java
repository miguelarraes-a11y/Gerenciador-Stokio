package com.senai.pi.stokio.configs;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "FABLAB patrimônio API",
        version = "1.0",
        description = "API responsável pelo gerenciamento e controle patrimonial do FABLAB, permitindo o cadastro, monitoramento e organização de equipamentos, ferramentas, materiais e ativos do laboratório."
    )
)
public class Swagger {
  
    
}
