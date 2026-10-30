/*
  eventos.uy - validaciones del formulario en el navegador (letra, 7.3).

  Esto NO reemplaza los controles de la logica: el navegador se puede
  saltear (desde la consola, con curl, deshabilitando el JavaScript), asi
  que Sistema vuelve a controlar lo mismo del lado del servidor. Lo que
  hace este archivo es avisarle al usuario antes de mandar el pedido, para
  que no pierda lo que ya escribio.

  El resto de las reglas del formulario (campos obligatorios, tipos,
  minimos y maximos) las controla el propio navegador con los atributos
  del HTML: required, type="email", type="date", min, step.
*/
(function () {
    "use strict";

    /* La contrasena y su confirmacion tienen que ser iguales.

       Se usa en dos pantallas:
         - alta de usuario, donde los dos campos son obligatorios;
         - modificar datos, donde los dos vacios significan "dejala como
           esta", y por eso dos vacios es un caso valido.

       setCustomValidity le pone al campo un mensaje de error propio. Con
       un mensaje distinto de vacio el navegador considera al campo
       invalido y no deja enviar el formulario; con el mensaje vacio el
       campo vuelve a ser valido. */
    function compararContrasenas() {
        var password = document.getElementById("pass");
        var confirmacion = document.getElementById("pass2");
        if (!password || !confirmacion) {
            return;
        }

        function revisar() {
            // Mismo texto que usa la logica, para que el usuario lea
            // siempre lo mismo venga el error de donde venga.
            confirmacion.setCustomValidity(
                password.value === confirmacion.value
                    ? ""
                    : "Las dos contraseñas no coinciden.");
        }

        // Se revisa mientras escribe en cualquiera de los dos campos: si
        // corrige el primero, el segundo tiene que dejar de estar en error.
        password.addEventListener("input", revisar);
        confirmacion.addEventListener("input", revisar);
        revisar();
    }

    compararContrasenas();
})();
