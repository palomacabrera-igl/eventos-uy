package servlets;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import logica.Fabrica;

/**
 * Se entera de cuando Tomcat levanta y apaga la aplicacion.
 *
 * Al apagarse le pide al Servidor Central que cierre su conexion con la base.
 * Si no, cada vez que se redespliega el WAR queda una conexion abierta contra
 * PostgreSQL.
 *
 * El pedido va por la Fabrica y no por la capa de persistencia: el modulo web
 * solo conoce la puerta del Servidor Central, nunca lo que hay adentro. Es la
 * misma regla que siguen los servlets con IControladorSistema.
 */
@WebListener
public class Arranque implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        Fabrica.cerrar();
    }
}
