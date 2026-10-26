package servlets;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import persistencia.Persistencia;

/**
 * Se entera de cuando Tomcat levanta y apaga la aplicacion.
 *
 * Al apagarse cierra el EntityManager y la fabrica de JPA. Si no, cada vez que
 * se redespliega el WAR queda una conexion abierta contra PostgreSQL.
 *
 * Es el unico lugar del modulo web que conoce a Persistencia, y es por el
 * ciclo de vida de la aplicacion, no por datos: lo mismo hace el main de la
 * Estacion de Trabajo con su shutdown hook.
 */
@WebListener
public class Arranque implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        Persistencia.cerrar();
    }
}
