package com.b2brk.web.rest.kcc.ws.dao;

import com.b2brk.ejb.exception.*;
import com.b2brk.web.rest.kcc.objects.UsuarioVO;
import com.b2brk.web.rest.kcc.ws.constante.Constantes;
import com.b2brk.web.rest.kcc.ws.utils.EncriptaPassword;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginTrackingDAO extends ComunDAO {

    public LoginTrackingDAO() {
    }
    //private SessionContext context;

    public UsuarioVO verificaUsuario(String login, String passwd) throws ErrorBaseDatos, ErrorDeAplicacion, PasswordIncorrecta, UsuarioBloqueado, UsuarioNoExiste {
        UsuarioVO usuario = new UsuarioVO();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String select = new String();
        try {
            select = "select u.usua_id, u.usua_login, u.usua_password "
                    + ", u.usua_nombre, u.usua_ap_paterno, u.usua_ap_materno "
                    + ", u.usua_ubicacion_prim, u.usua_habilitado "
                    + ", to_char(sysdate,'dd/MM/yyyy HH24:mi') as fecha "
                    + ", USUA_EMPR_ID, USUA_EMAIL, USUA_RUT, USUA_DV "
                    + " FROM "
                    + Constantes.ESQUEMA_B2_TRACKING + ".usuario u "
                    + " where u.usua_login =  ? ";

            con = this.getConnection();
            ps = con.prepareStatement(select);
            ps.setString(1, login);
            rs = ps.executeQuery();
            String clave = new String();
            if (rs.next()) {
                usuario.setIdUsua(rs.getInt("usua_id"));
                usuario.setNombre(rs.getString("usua_nombre"));
                String nombreFull = usuario.getNombre();
                if (rs.getString("usua_ap_paterno") != null) {
                    nombreFull += " " + rs.getString("usua_ap_paterno");
                }
                if (rs.getString("usua_ap_materno") != null) {
                    nombreFull += " " + rs.getString("usua_ap_materno");
                }
                usuario.setNombre(nombreFull);
                usuario.setIdEmpresa(rs.getInt("USUA_EMPR_ID"));
                clave = rs.getString("usua_password");
                if (rs.getString("usua_habilitado") != null && rs.getString("usua_habilitado").equals("N")) {
                    usuario.setBloqueado(true);
                } else {
                    usuario.setBloqueado(false);
                }
                //usuario.setIdUbicacionPrimaria(rs.getInt("usua_ubicacion_prim"));
            }
            ps.close();
            rs.close();
            if (con != null && !con.isClosed()) {
                con.close();
            }
            if (usuario.getIdUsua() == 0) {
                throw new UsuarioNoExiste("UsuarioNoExiste");
            } else {
                if (usuario.isBloqueado()) {
                    throw new UsuarioBloqueado("UsuarioBloqueado");
                }
                if (!this.comparaPassword(clave, passwd)) {
                    throw new PasswordIncorrecta("PasswordIncorrecta");
                }
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            throw new ErrorBaseDatos("" + sqle.getErrorCode());
        } catch (PasswordIncorrecta une) {
            throw une;
        } catch (UsuarioBloqueado une) {
            throw une;
        } catch (UsuarioNoExiste une) {
            throw une;
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new ErrorDeAplicacion("ERROR_DE_APLICACION");
        } finally {
            this.cierraCursores(ps, rs);
            this.cierraConeccion(con);
        }
        return usuario;
    }

    private boolean comparaPassword(String pass, String passComp) {
        EncriptaPassword enc = new EncriptaPassword();
        String compara = new String();
        try {
            compara = enc.getEncodedPassword(passComp);
        } catch (NoSuchAlgorithmException ex) {
            //Logger.getLogger(LoginTrackingDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pass.equals(compara);
    }

    public void setDatosEmpresa(UsuarioVO usuario) throws ErrorBaseDatos, ErrorDeAplicacion, EmpresaBloqueada {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String select = new String();
        try {
            select = "select e.empr_temp_id, e.empr_codigo, e.empr_nombre, e.empr_direccion, e.empr_rut, e.empr_dv "
                    + ", EMPR_HABILITADO from "
                    + Constantes.ESQUEMA_B2_TRACKING + ".empresas e "
                    + " where e.empr_id = ? ";

            con = this.getConnection();
            ps = con.prepareStatement(select);
            ps.setLong(1, usuario.getIdEmpresa());
            rs = ps.executeQuery();
            boolean empresaBloqueada = false;
            if (rs.next()) {
                usuario.setEmpresa(rs.getString("empr_codigo"));
                if (rs.getString("EMPR_HABILITADO") != null && rs.getString("EMPR_HABILITADO").equals("N")) {
                    empresaBloqueada = true;
                }
                if (rs.getInt("empr_temp_id") == Constantes.TIPO_EMPRESA_TRANSPORTE) {
                    usuario.setTransportista(true);
                }
            }
            ps.close();
            rs.close();
            if (con != null && !con.isClosed()) {
                con.close();
            }
            if (empresaBloqueada) {
                throw new EmpresaBloqueada("");
            }

        } catch (SQLException sqle) {
            sqle.printStackTrace();
            throw new ErrorBaseDatos("" + sqle.getErrorCode());
        } catch (EmpresaBloqueada ex) {
            throw new EmpresaBloqueada("");
        } catch (Throwable ex) {
            throw new ErrorDeAplicacion("ERROR_DE_APLICACION");
        } finally {
            this.cierraCursores(ps, rs);
            this.cierraConeccion(con);
        }
    }
}