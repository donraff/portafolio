/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccafast.utilitarios;

import cca.utils.Descifrar;
import ccafast.constante.Constantes;
import ccafast.dao.BaseDAO;
import ccafast.exceptions.ConexionCasillaException;
import ccafast.exceptions.EnvioCasillaException;
import ccafast.exceptions.ParametrosException;
import ccafast.objects.ArchivoVO;
import ccafast.objects.BancoVO;
import ccafast.objects.ParametrosVO;
import ccafast.objects.TipoArchivoVO;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

/**
 *
 * @author rvallejos
 */
public class Utils {

    private final static String DIR_APLICACION = "user.dir";
    private static final String ARCH_PROPIEDADES = "/CCAFAST.config";
    private final static String ARCH_PROP = System.getProperty(DIR_APLICACION).concat(ARCH_PROPIEDADES);
    //private final static String CP1252 = "Cp1252";
    private final static String UTILITARIO_EXCEP = "UtilsException";
    private final static int ZERO = 0;
    private static final int SIZE_MIN = 2048;
    private final static String FORMATO_FECHA = "yyyy-MM-dd";
    private final static String FORMATO_FECHA_RES = "yyyyMMddHHmmss";

    /**
     *
     * @param nombre
     * @param esPassword
     * @return
     * @throws ParametrosException
     */
    public static String obtenerPropiedad(String nombre, boolean esPassword) throws ParametrosException {
        String respuesta = "";
        try {
            Properties propiedades = getPropertieFile();
            respuesta = propiedades.getProperty(nombre);
            if (esPassword) {
                respuesta = Descifrar.descifrarCCA(propiedades.getProperty(nombre));
            }
        } catch (ParametrosException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        return respuesta;
    }

    /**
     *
     * Obtiene el Archivo de Propiedades
     *
     * @return
     * @throws ParametrosException
     */
    private static Properties getPropertieFile() throws ParametrosException {
        Properties propiedades = new Properties();
        try {
            String ruta = ARCH_PROP;
            ruta = Normalizer.normalize(ruta, Normalizer.Form.NFKC);
            if (ruta.matches("")) {
                throw new IllegalStateException();
            }
            try (FileInputStream fi = new FileInputStream(ruta)) {
                propiedades.load(fi);
            }
        } catch (IOException | IllegalArgumentException ex) {
            throw new ParametrosException(UTILITARIO_EXCEP);
        }
        return propiedades;
    }

    public static boolean verificaFirma(String entrada, File fileDato) {
        boolean resp = false;
        try {
            byte[] publicKeyEncoded = Files.readAllBytes(Paths.get(entrada.concat(fileDato.getName()).concat(Constantes.ARCH_KEY)));
            byte[] digitalSignature = Files.readAllBytes(Paths.get(entrada.concat(fileDato.getName()).concat(Constantes.ARCH_SIGN)));
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyEncoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            byte[] bytes = Files.readAllBytes(fileDato.toPath());
            signature.update(bytes);
            bytes = null;
            resp = signature.verify(digitalSignature);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resp;
    }


    

    public static void firmarArchivo(ArchivoVO archD, String entrada, File fileDato) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(SIZE_MIN, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            byte[] bytes = Files.readAllBytes(fileDato.toPath());
            signature.update(bytes);
            bytes = null;
            byte[] digitalSignature = signature.sign();
            archD.setFirma(digitalSignature);
            archD.setPublicKey(keyPair.getPublic().getEncoded());
            Files.write(Paths.get(entrada.concat(fileDato.getName()).concat(Constantes.ARCH_SIGN)), digitalSignature);
            Files.write(Paths.get(entrada.concat(fileDato.getName()).concat(Constantes.ARCH_KEY)), keyPair.getPublic().getEncoded());
            archD.setVerificaFirma(verificaFirma(entrada, fileDato));
            System.out.println("\t".concat(fileDato.getName()).concat(" Firmado"));
            System.out.println("\tVerifica Firma? " + archD.isVerificaFirma());
        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException | SignatureException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
}
