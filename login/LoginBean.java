/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.cca.portal.archivos.ejb.beans;

import cl.cca.portal.archivos.ejb.daos.ComunDAO;
import cl.cca.portal.archivos.ejb.daos.LoginDAO;
import cl.cca.portal.archivos.ejb.constantes.Constantes;
import cl.cca.portal.archivos.ejb.exceptions.BaseDatosException;
import cl.cca.portal.archivos.ejb.exceptions.AplicacionException;
import cl.cca.portal.archivos.ejb.exceptions.PasswordIncorrectaException;
import cl.cca.portal.archivos.ejb.exceptions.UsuarioBloqueadoException;
import cl.cca.portal.archivos.ejb.exceptions.UsuarioNoExisteException;
import cl.cca.portal.archivos.ejb.objects.AccesoVO;
import cl.cca.portal.archivos.ejb.objects.BuscadorVO;
import cl.cca.portal.archivos.ejb.objects.LogVO;
import cl.cca.portal.archivos.ejb.objects.PerfilVO;
import cl.cca.portal.archivos.ejb.objects.UsuarioVO;
import cl.cca.portal.archivos.ejb.utils.SendMailTLS;
import cl.cca.portal.archivos.ejb.utils.Utilitario;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 *
 * @author Administrador
 */
@Stateless(name = "Login", mappedName = "CCA/PortalContenidos/Login")
@TransactionManagement(TransactionManagementType.BEAN)
public class LoginBean implements LoginBeanLocal, Login {

    /**
     *
     */
    @Override
    public void test() {
        try {
            ComunDAO comun = new ComunDAO();
            Logger.getLogger(LoginBean.class.getName()).log(Level.INFO, String.valueOf(comun.obtenerFechaActual()));
            //Logger.getLogger(LoginBean.class.getName()).log(Level.INFO, String.valueOf(comun.listadoEmailCCAStar()));
        } catch (javax.ejb.EJBException ex) {
            Logger.getLogger(LoginBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BaseDatosException ex) {
            Logger.getLogger(LoginBean.class.getName()).log(Level.OFF, null, ex);
        }
    }

    /**
     *
     * @param buscador
     * @return
     * @throws BaseDatosException
     * @throws AplicacionException
     * @throws PasswordIncorrectaException
     * @throws UsuarioBloqueadoException
     * @throws UsuarioNoExisteException
     */
    @Override
    public UsuarioVO validarLogin(BuscadorVO buscador) throws BaseDatosException, AplicacionException, PasswordIncorrectaException, UsuarioBloqueadoException, UsuarioNoExisteException {
        LoginDAO login = new LoginDAO();
        UsuarioVO usuario = new UsuarioVO();
        try {
            usuario = login.verificaUsuario(buscador);
            LogVO log = Utilitario.generaLogVOLoginUsuario(usuario, buscador);
            if (usuario.getIdUsuario() == 0) {
                login.insertaLogUsuario(log, Constantes.LOG_INGRESO_FALLIDO_USUARIO_NO_EXISTE);
                throw new UsuarioNoExisteException("UsuarioNoExiste");
            } else if (usuario.getIdUsuario() > 0 && usuario.getIdEstadoUsuario() == Constantes.ESTADO_USUARIO_ELIMINADO_ID) {
                login.insertaLogUsuario(log, Constantes.LOG_INGRESO_FALLIDO_USUARIO_ELIMINADO);
                throw new UsuarioNoExisteException("UsuarioNoExiste");

            } else {
                login.listaPerfiles(usuario);
                login.listaOpcionMenu(usuario);
                if (usuario.getListaPerfiles().size() == 1) {
                    PerfilVO perfil = usuario.getListaPerfiles().get(0);
                    usuario.setIdPerfil(perfil.getId());
                    usuario.setPerfil(perfil.getDescripcion());
                    usuario.setPeusId(perfil.getIdPerfilUsuario());
                    usuario.setUltimoAcceso(login.ultimoAccesoUsuario(usuario).getFechaInicio());
                }
                log = Utilitario.generaLogVOLoginUsuario(usuario, buscador);

                if (usuario.isBloqueado()) {
                    login.insertaLogUsuario(log, Constantes.LOG_INGRESO_FALLIDO_USUARIO_BLOQUEADO);
                    throw new UsuarioBloqueadoException("UsuarioBloqueado");
                }
                if (!Utilitario.comparaPassword(usuario.getClave(), buscador.getPalabraClav())) {
                    login.descuentaIngresos(usuario);
                    int valor = usuario.getReintentos() - 1;
                    if (valor == 0) {
                        login.insertaLogUsuario(log, Constantes.LOG_BLOQUEO_USUARIO_INTENTOS_LOGIN_FALLIDOS);
                    } else {
                        login.insertaLogUsuario(log, Constantes.LOG_INGRESO_FALLIDO_PASSWORD_INCORRECTA);
                    }
                    throw new PasswordIncorrectaException("PasswordIncorrecta");
                }
            }
            login.insertaLogUsuario(log, Constantes.LOG_INGRESO_CORRECTO);
            if (usuario.getReintentos() < Constantes.CANTIDAD_REINTENTOS) {
                login.reseteaIngresos(usuario);
            }

        } catch (PasswordIncorrectaException | UsuarioBloqueadoException | UsuarioNoExisteException | BaseDatosException | AplicacionException p) {
            throw p;
        }
        return usuario;
    }

    /**
     *
     * @param usuario
     * @return
     * @throws BaseDatosException
     * @throws AplicacionException
     */
    @Override
    public UsuarioVO ingresarAcceso(UsuarioVO usuario) throws BaseDatosException, AplicacionException {
        LoginDAO login = new LoginDAO();
        try {
            usuario.setUltimoAcceso(login.ultimoAccesoUsuario(usuario).getFechaInicio());
            login.ingresarAcceso(usuario);
        } catch (BaseDatosException | AplicacionException p) {
            throw p;
        }
        return usuario;
    }

    /**
     *
     * @param usuario
     * @return
     * @throws BaseDatosException
     * @throws AplicacionException
     */
    @Override
    public ArrayList<AccesoVO> ultimosAccesosUsuario(UsuarioVO usuario) throws BaseDatosException, AplicacionException {
        LoginDAO login = new LoginDAO();
        return login.ultimosAccesosUsuario(usuario);
    }

    /**
     *
     * @param usuario
     * @throws BaseDatosException
     * @throws AplicacionException
     */
    @Override
    public void actualizarPasswordUsuario(UsuarioVO usuario) throws BaseDatosException, AplicacionException {
        LoginDAO login = new LoginDAO();
        try {
            login.actualizarPasswordUsuario(usuario, usuario.getIdUsuario(), false);
            SendMailTLS mail = new SendMailTLS();
            mail.notificaCambioPassword(usuario);
        } catch (BaseDatosException | AplicacionException e) {
            throw e;
        }
    }

    /**
     *
     * @param buscador
     * @throws BaseDatosException
     * @throws AplicacionException
     */
    @Override
    public void generaNuevoPasswordOlvido(BuscadorVO buscador) throws BaseDatosException, AplicacionException {
        LoginDAO login = new LoginDAO();
        try {
            UsuarioVO usuario = login.obtieneUsuarioResetPassword(buscador.getLogin());
            LogVO log = Utilitario.generaLogVOLoginUsuario(usuario, buscador);
            if (usuario.getIdUsuario() > 0 && usuario.getIdEstadoUsuario() != Constantes.ESTADO_USUARIO_ELIMINADO_ID) {
                usuario.setNuevaClave(Utilitario.generarPassword());
                login.actualizarPasswordUsuario(usuario, usuario.getIdUsuario(), true);
                SendMailTLS mail = new SendMailTLS();
                mail.enviaMensajeCambioPasswordPorReset(usuario);
                login.insertaLogUsuario(log, Constantes.LOG_SOLICITA_RESET_PASS);
            } else {
                login.insertaLogUsuario(log, Constantes.LOG_SOLICITA_RESET_PASS_NOK);
            }
        } catch (BaseDatosException | AplicacionException ex) {
            throw ex;
        } catch (NoSuchAlgorithmException ex) {
            throw new AplicacionException("");
        }
    }

    /**
     *
     * @param usuario
     * @param estado
     * @throws BaseDatosException
     * @throws AplicacionException
     */
    @Override
    public void actualizarAccesoUsuario(UsuarioVO usuario, String estado) throws BaseDatosException, AplicacionException {
        LoginDAO login = new LoginDAO();
        login.actualizarAccesoUsuario(usuario, estado);
    }
}
