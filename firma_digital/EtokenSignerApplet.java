package firmadigital;


import com.sun.org.apache.xml.internal.security.utils.Constants;

import java.applet.Applet;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;

import java.lang.reflect.Constructor;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.text.DateFormat;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JOptionPane;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignatureProperties;
import javax.xml.crypto.dsig.SignatureProperty;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class EtokenSignerApplet extends Applet {

    private static final String FIELD_PARAM_NUM_SOLICITUD = "numSolicitud";
    private static final String FIELD_PARAM_ID_TIPO_DOCUMENTO = "idTipoDoc";
    private static final String FIELD_PARAM_ID_XML_DOC = "idXML";
    private static final String FIELD_PARAM_TIPO_CONCESION = "tipoConcesion";
    private static final String FIELD_PARAM_FIRMANTE = "firmante";

    //private static final String FIELD_PARAM_LINE = "line";
    private static final String SIGN_BUTTON_CAPTION_PARAM = "signButtonCaption";

    private static final String PKCS11_KEYSTORE_TYPE = "PKCS11";
    private static final String X509_CERTIFICATE_TYPE = "X.509";
    private static final String CERTIFICATION_CHAIN_ENCODING = "PkiPath";
    //private static final String DIGITAL_SIGNATURE_ALGORITHM_NAME = "SHA1withRSA";
    private static final String SUN_PKCS11_PROVIDER_CLASS = "sun.security.pkcs11.SunPKCS11";

    private Button mSignButton;
    String newRut;

    /*
     * Initializes the applet - creates and initializes its graphical user interface.
     * Actually the applet consists of a single button, that fills its all surface. The
     * button's caption is taken from the applet parameter SIGN_BUTTON_CAPTION_PARAM.
     */
    public void init() {
        System.out.println("Sign rvallejos SEC V 2012-04-25");
        String signButtonCaption = this.getParameter(SIGN_BUTTON_CAPTION_PARAM);
        mSignButton = new Button(signButtonCaption);
        mSignButton.setLocation(0, 0);
        Dimension appletSize = this.getSize();
        mSignButton.setSize(appletSize);
        mSignButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                signXML();
            }
        });
        this.setLayout(null);
        this.add(mSignButton);
    }

    private void signXML() {
        System.out.println("signXML...");
        try {
            JSObject browserWindow = JSObject.getWindow(this);
            JSObject mainForm = (JSObject) browserWindow.eval("document.forms[1]");

            JSObject numSolicitudField = (JSObject) mainForm.getMember(this.getParameter(FIELD_PARAM_NUM_SOLICITUD));
            String numSolicitud = (String) numSolicitudField.getMember("value");

            JSObject idTipoDocField = (JSObject) mainForm.getMember(this.getParameter(FIELD_PARAM_ID_TIPO_DOCUMENTO));
            String idTipoDoc = (String) idTipoDocField.getMember("value");

            JSObject idXMLField = (JSObject) mainForm.getMember(this.getParameter(FIELD_PARAM_ID_XML_DOC));
            String idXML = (String) idXMLField.getMember("value");

            JSObject tipoConcesionField = (JSObject) mainForm.getMember(this.getParameter(FIELD_PARAM_TIPO_CONCESION));
            String tipoConcesion = (String) tipoConcesionField.getMember("value");

            JSObject firmanteField = (JSObject) mainForm.getMember(this.getParameter(FIELD_PARAM_FIRMANTE));
            String firmante = (String) firmanteField.getMember("value");
/*
            "<PARAM name=\"numSolicitud\" value=\"numSolicitud\">\n" +
            "<PARAM name=\"idTipoDoc\" value=\"idTipoDoc\">\n" +
            "<PARAM name=\"idXML\" value=\"idXML\">\n" +
            "<PARAM name=\"tipoConcesion\" value=\"tipoConcesion\">\n" +
            "<PARAM name=\"firmante\" value=\"firmante\">\n" +
*/
            CertificationChainAndSignatureBase64 signingResult = signXMLFile(numSolicitud, idTipoDoc, idXML, tipoConcesion, firmante);
            if (signingResult != null) {
                JOptionPane.showMessageDialog(this, "Documento Firmado Correctamente");
                mSignButton.setLabel("Documento Firmado Correctamente");
                mSignButton.setEnabled(false);
                System.out.println("Documento Firmado Correctamente");
            } else {
                // User canceled signing
            }
        }
        catch (DocumentSignException dse) {
            // Document signing failed. Display error message
            String errorMessage = dse.getMessage();
            JOptionPane.showMessageDialog(this, errorMessage);
        }
        catch (SecurityException se) {
            se.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "No se puede tener acceso al sistema de archivos local. \n" +
                "Esta aplicación se debe iniciar con permisos de seguridad. \n" +
                "Por favor, acepte a confiar en el applet cuando el Java Plug-In lo solicite.");
        }catch (JSException jse) {
            jse.printStackTrace();
            JOptionPane.showMessageDialog(this,
            "No se puede acceder a algunos de los campos del \n" +
            "formulario HTML. Por favor, compruebe los parámetros del applet..");
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error inesperado: " + e.getMessage());
        }

    }
    
    private CertificationChainAndSignatureBase64 signXMLFile(String numSolicitud, String idTipoDoc, String idXML, String tipoConcesion, String firmante)
    throws DocumentSignException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        org.w3c.dom.Document xmlDoc = null;
        try{
            builder = factory.newDocumentBuilder();
        }catch(Exception e){
            e.printStackTrace();
        }
        try {
            URLConnection con = getServletConnection("Firma2");
            java.io.OutputStream outstream = con.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(outstream);
            oos.writeObject("leer");
            oos.writeObject(numSolicitud);
            oos.writeObject(idTipoDoc);
            oos.writeObject(idXML);
            oos.writeObject(tipoConcesion);
            
            oos.flush();
            oos.close();
            InputStream instr = con.getInputStream();
            ObjectInputStream inputFromServlet = new ObjectInputStream(instr);
            StringBuffer sb = new StringBuffer(inputFromServlet.readObject().toString());
            if(sb != null)
            {
                StringReader sr = new StringReader(sb.toString());
                xmlDoc = builder.parse(new InputSource(sr));
            }            
            inputFromServlet.close();
            instr.close();            
        }catch(SAXException s){
            String errorMessage = "Error de Comunicación interno.";
            throw new DocumentSignException(errorMessage, s);
        }catch(ClassNotFoundException n){
            String errorMessage = "Error de Comunicación interno.";
            throw new DocumentSignException(errorMessage, n);
        } catch (IOException ioex) {
            String errorMessage = "No se encuentra Documento para firmar.";
            throw new DocumentSignException(errorMessage, ioex);
        }

        // Show a dialog for choosing PKCS#11 implementation library and smart card PIN
        PKCS11LibraryFileAndPINCodeDialog pkcs11Dialog = new PKCS11LibraryFileAndPINCodeDialog();
        boolean dialogConfirmed;
        try {
            dialogConfirmed = pkcs11Dialog.run();
        } finally {
            pkcs11Dialog.dispose();
        }

        if (dialogConfirmed) {
            String oldButtonLabel = mSignButton.getLabel();
            mSignButton.setLabel("Firmando Documento...");
            mSignButton.setEnabled(false);
            try {
                UtilitariosFirma util = new UtilitariosFirma();
                //String pkcs11LibraryFileName = pkcs11Dialog.getLibraryFileName();
                String pkcs11LibraryFileName = util.pkcs11LibraryFileName();
                String pinCode = pkcs11Dialog.getSmartCardPINCode();
                //System.out.println("osName: "+osName);
                // Do the actual signing of the document with the smart card
                CertificationChainAndSignatureBase64 signingResult =  signXMLDocument(xmlDoc, pkcs11LibraryFileName, pinCode);
                org.w3c.dom.Document xmlDocSigned = signingResult.xmlDocSigned;
                //Guardado XML
                URLConnection con = getServletConnection("Firma2");
                OutputStream outstream = con.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(outstream);
                oos.writeObject("escribir");
                oos.writeObject(numSolicitud);
                oos.writeObject(idTipoDoc);
                oos.writeObject(idXML);
                oos.writeObject(tipoConcesion);
                oos.writeObject(firmante);
                oos.writeObject(util.DOM2String(xmlDocSigned));
                oos.flush();
                oos.close();
                InputStream instr = con.getInputStream();
                ObjectInputStream inputFromServlet = new ObjectInputStream(instr);
                String status = (String)inputFromServlet.readObject();
                if(status.equals("0"))
                {
                    inputFromServlet.close();
                    instr.close();
                }
                if(status.equals("45"))
                {
                    inputFromServlet.close();
                    instr.close();
                    String errorMessage = "Ha orurrido un error al almacenar el  archivo con la nueva Firma.";
                    throw new DocumentSignException(errorMessage, new Exception());
                }
                inputFromServlet.close();
                instr.close();
                return signingResult;

            }catch(ClassNotFoundException n){
                String errorMessage = "Error de Comunicación interno.";
                throw new DocumentSignException(errorMessage, n);
            } catch (IOException ioex) {
                String errorMessage = "No se encuentra Documento para firmar.";
                throw new DocumentSignException(errorMessage, ioex);
                
            } catch (DocumentSignException ioex) {
                    //String errorMessage = "Error al firmar Documento.";
                    //throw new DocumentSignException(errorMessage, ioex);
                throw ioex;
            } finally {
                mSignButton.setLabel(oldButtonLabel);
                mSignButton.setEnabled(true);
            }
        }
        else {
            return null;
        }
    }
    private CertificationChainAndSignatureBase64 signXMLDocument(org.w3c.dom.Document xmlDoc, String aPkcs11LibraryFileName, String aPinCode) throws DocumentSignException {
        //este es el firmador final
        if (aPkcs11LibraryFileName.length() == 0) {
            String errorMessage = "¡No se encuentran archivos de configuracion PCKS#11 " +
                "para dispositivo eToken (archivo .dll o .so)!";
            throw new DocumentSignException(errorMessage);
        }
        // Load the keystore from the smart card using the specified PIN code
        KeyStore userKeyStore = null;
        try {
            userKeyStore = loadKeyStoreFromSmartCard(aPkcs11LibraryFileName, aPinCode);
        } catch (KeyStoreException ex) {
            String errorMessage = "No se puede leer el almacén de claves del eToken.\n" +
                "Posibles razones:\n" +
                " - Dispositivo eToken no está conectado.\n" +
                " - Implementación de librerías PKCS#11 es invalida.";
            throw new DocumentSignException(errorMessage, ex);
        } catch (GeneralSecurityException ex) {
            String errorMessage = "No se puede leer el almacén de claves del eToken.\n" +
                "Posible razón:\n" +
                " - Password de eToken incorrecto.\n";
            throw new DocumentSignException(errorMessage, ex);
        } catch (Exception ex) {
            String errorMessage = "No se puede leer el almacén de claves del eToken.\n" +
                "Posibles razones:\n" +
                " - Dispositivo eToken no está conectado.\n" +
                " - Implementación de librerías PKCS#11 es invalida.\n" +
                " - Password de eToken es incorrecto.\n" +
                "Detalle Problema: '" + ex.getMessage()+"'";
            throw new DocumentSignException(errorMessage, ex);
        }

        // Get the private key and its certification chain from the keystore
        PrivateKeyAndCertChain privateKeyAndCertChain = null;
        try {
            privateKeyAndCertChain = getPrivateKeyAndCertChain(userKeyStore);
        } catch (GeneralSecurityException gsex) {
            String errorMessage = "No se puede extraer clave privada y " +
                "certificado desde eToken. Razón: " + gsex.getMessage();
            throw new DocumentSignException(errorMessage, gsex);
        }

        // Check if the private key is available
        PrivateKey privateKey = privateKeyAndCertChain.mPrivateKey;
        if (privateKey == null) {
            String errorMessage = "No se encuentra clave privada en eToken.";
            throw new DocumentSignException(errorMessage);
        }

        // Check if X.509 certification chain is available
        Certificate[] certChain = privateKeyAndCertChain.mCertificationChain;
        if (certChain == null) {
            String errorMessage = "No se encuentra certificado en eToken.";
            throw new DocumentSignException(errorMessage);
        }

        // Create the result object
        CertificationChainAndSignatureBase64 signingResult = new CertificationChainAndSignatureBase64();
        // Save X.509 certification chain in the result encoded in Base64
        try {
            signingResult.mCertificationChain = encodeX509CertChainToBase64(certChain);
        }
        catch (CertificateException cee) {
            String errorMessage = "Certificado de eToken inválido.";
            throw new DocumentSignException(errorMessage);
        }
        try {
            UtilitariosFirma util = new UtilitariosFirma();
            org.apache.xml.security.Init.init();
            xmlDoc.normalize();
            X509Certificate cert = null;
            for(Enumeration aliasesEnum = userKeyStore.aliases(); aliasesEnum.hasMoreElements();){
                String alias = (String)aliasesEnum.nextElement();
                cert = (X509Certificate)userKeyStore.getCertificate(alias);
                boolean validaFecha = util.validarFechas(cert);
                boolean revocado = util.compruebaRevocacion(cert);
                if(!validaFecha){//validaFecha OK estado: true
                    String errorMessage = "Falló proceso de Firma.\n" +
                        "Detalle del Problema: Certificado ha expirado.";
                    throw new DocumentSignException(errorMessage, new Exception());
                }
                if(revocado){//revocado OK estado: false
                    String errorMessage = "Falló proceso de Firma.\n" +
                        "Detalle del Problema: Certificado Revocado.";
                    throw new DocumentSignException(errorMessage, new Exception());
                }
            }
            //Element signer = getInfo(xmlDoc, cert);
            System.out.println("xmlDoc lenght: "+util.DOM2String(xmlDoc).length());
            org.w3c.dom.Document xmlDocSigned = firmar(xmlDoc, cert, privateKey);
            System.out.println("xmlDocSigned lenght: "+util.DOM2String(xmlDocSigned).length());
            signingResult.xmlDocSigned = xmlDocSigned;
        } catch (GeneralSecurityException gsex) {
            String errorMessage = "Falló proceso de Firma.\n" +
                "Detalle del Problema: " + gsex.getMessage();
            throw new DocumentSignException(errorMessage, gsex);
        } catch (Exception gsex) {
            String errorMessage = "Falló proceso de Firma.\n" +
                "Detalle del Problema: " + gsex.getMessage();
            throw new DocumentSignException(errorMessage, gsex);
        }
        return signingResult;
    }

    public org.w3c.dom.Document firmar(org.w3c.dom.Document xmlDoc, X509Certificate cert, PrivateKey privateKey) throws Exception, DocumentSignException
    {
        //DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //dbf.setNamespaceAware(true);
        //DocumentBuilder db = dbf.newDocumentBuilder();
        try
        {
            NodeList nl = xmlDoc.getElementsByTagName("Content");
            if(nl.getLength() < 1)
            {
                String errorMessage = "Falló proceso de Firma.\n" +
                    "Detalle del Problema: Documento No válido para proceso de firma.";
                throw new DocumentSignException(errorMessage, new Exception());
            }
            if(nl.getLength() > 1)
            {
                //msg = (String)msgError.get("21");
                String errorMessage = "Falló proceso de Firma.\n" +
                    "Detalle del Problema: Documento No válido para proceso de firma.";
                throw new DocumentSignException(errorMessage, new Exception());
            }
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            // XPath filtering for multiple enveloped signatures support
            Transform transform = null;
            transform = fac.newTransform(Transform.XPATH, new XPathFilterParameterSpec("not(ancestor-or-self::dsig:Signature)", Collections.singletonMap("dsig", javax.xml.crypto.dsig.XMLSignature.XMLNS)));
            //transform = fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);      
            Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA1, null),Collections.singletonList(transform), null, null);
            // Create the SignedInfo.
            SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));
            // Create the KeyInfo containing the X509Data.
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            List<Object> x509Content = new ArrayList<Object>();
            //x509Content.add(cert.getSubjectX500Principal().getName());
            x509Content.add(cert.getSubjectDN().getName());
            //x509Content.add("aaaa");
            x509Content.add(cert);
            X509Data xd = kif.newX509Data(x509Content);
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

            NodeList nodo = xmlDoc.getElementsByTagNameNS(javax.xml.crypto.dsig.XMLSignature.XMLNS, "Signature");

            // Contenido de SignatureProperty
            Element content = xmlDoc.createElement("dc:date");
            content.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss,SS");
            content.setTextContent(sdf.format(new Date()));
            XMLStructure str = new DOMStructure(content);
            List<XMLStructure> contentList = new ArrayList<XMLStructure>();
            contentList.add(str);
            
            // SignatureProperty
            SignatureProperty sp = fac.newSignatureProperty(contentList, "S"+nodo.getLength(), null);
            List<SignatureProperty> spList = new ArrayList<SignatureProperty>();
            spList.add(sp);

            // SignatureProperties
            SignatureProperties sps = fac.newSignatureProperties(spList, null);
            List<SignatureProperties> spsList = new ArrayList<SignatureProperties>();
            spsList.add(sps);

            // Object
            XMLObject object = fac.newXMLObject(spsList, null, null, null);
            List<XMLObject> objectList = new ArrayList<XMLObject>();
            objectList.add(object);

            javax.xml.crypto.dsig.XMLSignature signature = fac.newXMLSignature(si, ki, objectList, "S"+nodo.getLength(), null);
            DOMSignContext signContext = new DOMSignContext(privateKey, xmlDoc.getDocumentElement());
            //DOMSignContext signContext = new DOMSignContext(privateKey, xmlDoc.getElementsByTagName("Content").item(0));
            signContext.putNamespacePrefix(javax.xml.crypto.dsig.XMLSignature.XMLNS, "ds");
            signature.sign(signContext);
            


            //String BaseURI = "#data";



/*
            String BaseURI = "";
            XMLSignature sig = new XMLSignature(xmlDoc, BaseURI, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);
            
            nl.item(0).appendChild(sig.getElement());
            xmlDoc.getDocumentElement().appendChild(sig.getElement());
            Transforms transforms = new Transforms(xmlDoc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);  
            sig.addDocument(BaseURI, transforms, Constants.ALGO_ID_DIGEST_SHA1);
            sig.addKeyInfo(cert);  
            sig.addKeyInfo(cert.getPublicKey());  
            sig.sign(privateKey);

            if(sig.getSignedInfo().verify()){
                return xmlDoc;
            } else
            {
                return null;
            }
*/
            return xmlDoc;
        }
        catch(DocumentSignException d){
            throw d;
        }
        catch(Exception e)
        {
            System.out.println("ERROR en firmar");
            e.printStackTrace();
            return null;
        }
        //return xmlDoc;
    }

    public org.w3c.dom.Document firmar_20111(org.w3c.dom.Document xmlDoc, Element signer, X509Certificate cert, PrivateKey privateKey) throws Exception, DocumentSignException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        try
        {
            //String BaseURI = "#data";
            String BaseURI = "";
            XMLSignature sig = new XMLSignature(xmlDoc, BaseURI, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);
            NodeList nl = xmlDoc.getElementsByTagName("Content");
            if(nl.getLength() < 1)
            {
                String errorMessage = "Falló proceso de Firma.\n" +
                    "Detalle del Problema: Documento No válido para proceso de firma.";
                throw new DocumentSignException(errorMessage, new Exception());
            }
            if(nl.getLength() > 1)
            {
                //msg = (String)msgError.get("21");
                String errorMessage = "Falló proceso de Firma.\n" +
                    "Detalle del Problema: Documento No válido para proceso de firma.";
                throw new DocumentSignException(errorMessage, new Exception());
            }
            
            nl.item(0).appendChild(sig.getElement());
            xmlDoc.getDocumentElement().appendChild(sig.getElement());
            Transforms transforms = new Transforms(xmlDoc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);  
            sig.addDocument(BaseURI, transforms, Constants.ALGO_ID_DIGEST_SHA1);
/*            
            //Agrega tags firmante
            NodeList nlSigners = signer.getChildNodes();
            for(int l = 0; l < nlSigners.getLength(); l++){
                Element dato = (Element)nlSigners.item(l);
                sig.addTextElement( dato.getTextContent(), "signer_"+dato.getTagName());
            }
*/
/*
// esto no rompe la firma, agrega tags signer fuera del <Content>:
                NodeList nlSigners = xmlDoc.getElementsByTagName("signers");
                if(nlSigners.getLength() == 0)
                {
                    xmlDoc.getDocumentElement().appendChild(xmlDoc.createElement("signers"));
                    nlSigners = xmlDoc.getElementsByTagName("signers");
                }
                nlSigners.item(0).appendChild(signer);
                xmlDoc.normalize();
            //}
*/

            sig.addKeyInfo(cert);  
            sig.addKeyInfo(cert.getPublicKey());  
            sig.sign(privateKey);
            if(sig.getSignedInfo().verify()){
                return xmlDoc;
            } else
            {
                return null;
            }
        }
        catch(DocumentSignException d){
            throw d;
        }
        catch(Exception e)
        {
            System.out.println("ERROR en firmar");
            e.printStackTrace();
            return null;
        }
        //return xmlDoc;
    }



    public org.w3c.dom.Document firmar_0(org.w3c.dom.Document xmlDoc, Element signer, Vector firmantes, X509Certificate cert, PrivateKey privateKey) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        try
        {
            String BaseURI = "#data";
            XMLSignature sig = new XMLSignature(xmlDoc, BaseURI, XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);
            NodeList nl = xmlDoc.getElementsByTagName("Content");
            if(nl.getLength() < 1)
            {
                //msg = (String)msgError.get("21");
                return null;
            }
            if(nl.getLength() > 1)
            {
                //msg = (String)msgError.get("21");
                return null;
            }
            nl.item(0).appendChild(sig.getElement());
            String Id = "S" + (firmantes.size() + 1);
            Transforms transforms = new Transforms(xmlDoc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);  
            sig.addDocument(BaseURI, transforms, Constants.ALGO_ID_DIGEST_SHA1);

            sig.setId(Id);
            sig.addKeyInfo(cert);
            sig.addKeyInfo(cert.getPublicKey());
            sig.sign(privateKey);
            if(sig.getSignedInfo().verify()){
                NodeList nlSigners = xmlDoc.getElementsByTagName("signers");
                if(nlSigners.getLength() == 0){
                    xmlDoc.getDocumentElement().appendChild(xmlDoc.createElement("signers"));
                    nlSigners = xmlDoc.getElementsByTagName("signers");
                }
                nlSigners.item(0).appendChild(signer);
                xmlDoc.normalize();
            } else
            {
                return null;
            }
        }
        catch(Exception e)
        {
            System.out.println("ERROR en firmar");
            e.printStackTrace();
            return null;
        }
        return xmlDoc;
    }


    /*
     * Loads the keystore from the smart card using its PKCS#11 implementation
     * library and the Sun PKCS#11 security provider. The PIN code for accessing
     * the smart card is required.
     */
    private KeyStore loadKeyStoreFromSmartCard(String aPKCS11LibraryFileName, String aSmartCardPIN)
    throws GeneralSecurityException, IOException, KeyStoreException {
        String pkcs11ConfigSettings = "name = SmartCard\n" + "library = " + aPKCS11LibraryFileName;
        byte[] pkcs11ConfigBytes = pkcs11ConfigSettings.getBytes();
        ByteArrayInputStream confStream = new ByteArrayInputStream(pkcs11ConfigBytes);
        try {
            Class sunPkcs11Class = Class.forName(SUN_PKCS11_PROVIDER_CLASS);
            Constructor pkcs11Constr = sunPkcs11Class.getConstructor(java.io.InputStream.class);
            Provider pkcs11Provider = (Provider) pkcs11Constr.newInstance(confStream);
            Security.addProvider(pkcs11Provider);
        } catch (Exception e) {
            throw new KeyStoreException("No inicializa Sun PKCS#11 security provider. Razón: " + e.getCause().getMessage());
        }
       // Read the keystore form the smart card
        char[] pin = aSmartCardPIN.toCharArray();
        KeyStore keyStore = KeyStore.getInstance(PKCS11_KEYSTORE_TYPE);
        try{
            keyStore.load(null, pin);
        }catch(java.io.IOException e){
            throw new GeneralSecurityException("Password inválida");
        }
        return keyStore;
    }

    /*
     * @return private key and certification chain corresponding to it, extracted from
     * given keystore. The keystore is considered to have only one entry that contains
     * both certification chain and its corresponding private key. If the keystore has
     * no entries, an exception is thrown.
     */
    private PrivateKeyAndCertChain getPrivateKeyAndCertChain(
        KeyStore aKeyStore)
    throws GeneralSecurityException {
        Enumeration aliasesEnum = aKeyStore.aliases();
        if (aliasesEnum.hasMoreElements()) {
            String alias = (String)aliasesEnum.nextElement();
            Certificate[] certificationChain = aKeyStore.getCertificateChain(alias);
            PrivateKey privateKey = (PrivateKey) aKeyStore.getKey(alias, null);
            PrivateKeyAndCertChain result = new PrivateKeyAndCertChain();
            result.mPrivateKey = privateKey;
            result.mCertificationChain = certificationChain;
            return result;
        } else {
            throw new KeyStoreException("Alamacén de Claves vacío!");
        }
    }

    /*
     * @return Base64-encoded ASN.1 DER representation of given X.509 certification
     * chain.
     */
    private String encodeX509CertChainToBase64(Certificate[] aCertificationChain)
    throws CertificateException {
        List certList = Arrays.asList(aCertificationChain);
        CertificateFactory certFactory =
            CertificateFactory.getInstance(X509_CERTIFICATE_TYPE);
        CertPath certPath = certFactory.generateCertPath(certList);
        byte[] certPathEncoded = certPath.getEncoded(CERTIFICATION_CHAIN_ENCODING);
        String base64encodedCertChain = Base64Utils.base64Encode(certPathEncoded);
        return base64encodedCertChain;
    }




    /*
     * Data structure that holds a pair of private key and
     * certification chain corresponding to this private key.
     */
    static class PrivateKeyAndCertChain {
        public PrivateKey mPrivateKey;
        public Certificate[] mCertificationChain;
    }

    /*
     * Data structure that holds a pair of Base64-encoded
     * certification chain and digital signature.
     */
    static class CertificationChainAndSignatureBase64 {
        public String mCertificationChain = null;
        public String mSignature = null;
        public org.w3c.dom.Document xmlDocSigned = null;
    }

    /*
     * Exception class used for document signing errors.
     */
    static class DocumentSignException extends Exception {
        public DocumentSignException(String aMessage) {
            super(aMessage);
        }

        public DocumentSignException(String aMessage, Throwable aCause) {
            super(aMessage, aCause);
        }
    }


    private URLConnection getServletConnection(String nombre) throws MalformedURLException, IOException 
    {
        URL urlServlet = new URL(getCodeBase(), nombre);
        URLConnection con = urlServlet.openConnection();
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setRequestProperty("Content-Type", "application/x-java-serialized-object");
        return con;
    }

}