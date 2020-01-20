package firmadigital;

import java.applet.Applet;

import java.awt.Button;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StringReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class VisadoSignerApplet extends Applet{
    private static final String FIELD_PARAM_NUM_SOLICITUD = "numSolicitud";
    private static final String FIELD_PARAM_ID_TIPO_DOCUMENTO = "idTipoDoc";
    private static final String FIELD_PARAM_ID_XML_DOC = "idXML";
    private static final String FIELD_PARAM_TIPO_CONCESION = "tipoConcesion";
    private static final String FIELD_PARAM_FIRMANTE = "firmante";
    private static final String FIELD_PARAM_PASSWD = "pEnc";
    private static final String FIELD_PARAM_PERFIL = "perfil";
    private static final String SIGN_BUTTON_CAPTION_PARAM = "signButtonCaption";
    private Button mSignButton;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    
    public void init() {
        System.out.println("Visado rvallejos SEC V 2012-01-25");
        String signButtonCaption = this.getParameter(SIGN_BUTTON_CAPTION_PARAM);
        mSignButton = new Button(signButtonCaption);
        mSignButton.setLocation(0, 0);
        Dimension appletSize = this.getSize();
        mSignButton.setSize(appletSize);
        mSignButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                visarXML();
            }
        });
        this.setLayout(null);
        this.add(mSignButton);
    }

    private void visarXML() {
        System.out.println("visarXML...");
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

            JSObject passwdField = (JSObject) mainForm.getMember(this.getParameter(FIELD_PARAM_PASSWD));
            String passwd = (String) passwdField.getMember("value");

            JSObject perfilField = (JSObject) mainForm.getMember(this.getParameter(FIELD_PARAM_PERFIL));
            String perfil = (String) perfilField.getMember("value");


            CertificationChainAndSignatureBase64 signingResult = signXMLFile(numSolicitud, idTipoDoc, idXML, tipoConcesion, firmante, passwd, perfil);
            if (signingResult != null) {
                JOptionPane.showMessageDialog(this, "Documento Visado Correctamente");
                mSignButton.setLabel("Documento Visado Correctamente");
                mSignButton.setEnabled(false);
                System.out.println("Documento Visado Correctamente");
            } else {
                // User canceled signing
                JOptionPane.showMessageDialog(this, "Documento No Visado");
            }
        }
        catch (DocumentSignException dse) {
            // Document signing failed. Display error message
            String errorMessage = dse.getMessage();
            JOptionPane.showMessageDialog(this, errorMessage);
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
    private CertificationChainAndSignatureBase64 signXMLFile(String numSolicitud, String idTipoDoc, String idXML, String tipoConcesion, String firmante, String pass, String perfil)
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
        VisadoPINCodeDialog pinDialog = new VisadoPINCodeDialog();
        boolean dialogConfirmed;
        try {
            dialogConfirmed = pinDialog.run();
        } finally {
            pinDialog.dispose();
        }

        if (dialogConfirmed) {
            String oldButtonLabel = mSignButton.getLabel();
            mSignButton.setLabel("Visando Documento...");
            mSignButton.setEnabled(false);
            try {
                UtilitariosFirma util = new UtilitariosFirma();
                String pinCode = pinDialog.getSmartCardPINCode();
                URLConnection con = getServletConnection("Firma2");
                OutputStream outstream = con.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(outstream);
                oos.writeObject("datosUsuario");
                oos.writeObject(firmante);
                oos.writeObject(perfil);
                oos.flush();
                oos.close();
                InputStream instr = con.getInputStream();
                ObjectInputStream inputFromServlet = new ObjectInputStream(instr);
                VisadoAntVO datoUsuario = new VisadoAntVO();
                datoUsuario.setIdPerfil(perfil);
                datoUsuario.setRut(firmante);
                datoUsuario.setNombre((String)inputFromServlet.readObject());
                datoUsuario.setPaterno((String)inputFromServlet.readObject());
                datoUsuario.setMaterno((String)inputFromServlet.readObject());
                datoUsuario.setPerfilSTR((String)inputFromServlet.readObject());
                datoUsuario.setIniciales((String)inputFromServlet.readObject());
                datoUsuario.setDv((String)inputFromServlet.readObject());
                System.out.println("xmlDoc lenght: "+util.DOM2String(xmlDoc).length());
                CertificationChainAndSignatureBase64 signingResult =  signXMLDocument(xmlDoc, pinCode, pass, datoUsuario);
                System.out.println("xmlDoc lenght: "+util.DOM2String(signingResult.xmlDocSigned).length());



                org.w3c.dom.Document xmlDocSigned = signingResult.xmlDocSigned;
                if(xmlDocSigned != null){
                    //Guardado XML
                    con = getServletConnection("Firma2");
                    outstream = con.getOutputStream();
                    oos = new ObjectOutputStream(outstream);
                    oos.writeObject("escribir");
                    oos.writeObject(numSolicitud);
                    oos.writeObject(idTipoDoc);
                    oos.writeObject(idXML);
                    oos.writeObject(tipoConcesion);
                    oos.writeObject(firmante);
                    oos.writeObject(util.DOM2String(xmlDocSigned));
                    oos.flush();
                    oos.close();
                    instr = con.getInputStream();
                    inputFromServlet = new ObjectInputStream(instr);
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
                        String errorMessage = "Ha orurrido un error al almacenar el  archivo con el nuevo Visado.";
                        throw new DocumentSignException(errorMessage, new Exception());
                    }
                }
                inputFromServlet.close();
                instr.close();
                return signingResult;


                } catch (IOException ioex) {
                    String errorMessage = "No se encuentra Registro de Usuario.";
                    throw new DocumentSignException(errorMessage, ioex);
                
            }catch(ClassNotFoundException n){
                String errorMessage = "Error de Comunicación interno.";
                throw new DocumentSignException(errorMessage, n);
                
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

    private CertificationChainAndSignatureBase64 signXMLDocument(org.w3c.dom.Document xmlDoc, String aPinCode, String pass, VisadoAntVO datoUsuario) throws DocumentSignException {
        //este es el firmador final
        try {
            if(!pass.equals(this.getEncodedPassword(aPinCode))){
                throw new DocumentSignException("");
            }
        } catch (NoSuchAlgorithmException gsex) {
            String errorMessage = "Password de Usuario no corresponde a la de su registro.";
            throw new DocumentSignException(errorMessage);
        } catch (DocumentSignException gsex) {
            throw gsex;
        }
        // Create the result object
        CertificationChainAndSignatureBase64 signingResult = new CertificationChainAndSignatureBase64();
        try {
            //----------------------
            UtilitariosFirma util = new UtilitariosFirma();
            NodeList contenidoNL = xmlDoc.getElementsByTagName("Content");
            NodeList visadosNL = xmlDoc.getElementsByTagName("Visados");
            int cantVisados = visadosNL.getLength();
            Element contenidoE = (Element)contenidoNL.item(0);
            if(cantVisados == 0){
                contenidoE.appendChild(xmlDoc.createElement("Visados"));
            }
            visadosNL = xmlDoc.getElementsByTagName("Visados");
            cantVisados = visadosNL.getLength();
            Element visadosE = (Element)visadosNL.item(0);
            visadosE.appendChild(xmlDoc.createElement("visado"));
            NodeList visadoNL = xmlDoc.getElementsByTagName("visado");
            boolean existeVisado = false;

            if(visadoNL.getLength() > 1){
                existeVisado = this.existeVisadoPrevio(datoUsuario.getRut(), datoUsuario.getIdPerfil(), visadosNL);
            }
            if(!existeVisado){
                Element visadoE = (Element)visadoNL.item(visadoNL.getLength()-1);
                visadoE.setAttribute("id", "V"+(visadoNL.getLength()-1));
                visadoE.appendChild(UtilitariosFirma.createNode(xmlDoc, "nombres",   datoUsuario.getNombre()));
                visadoE.appendChild(UtilitariosFirma.createNode(xmlDoc, "apellidoPaterno",   datoUsuario.getPaterno()));  
                visadoE.appendChild(UtilitariosFirma.createNode(xmlDoc, "apellidoMaterno",   datoUsuario.getMaterno()));  
                visadoE.appendChild(UtilitariosFirma.createNode(xmlDoc, "iniciales",   datoUsuario.getIniciales()));  
                visadoE.appendChild(UtilitariosFirma.createNode(xmlDoc, "rut",   datoUsuario.getRut()+""));  
                visadoE.appendChild(UtilitariosFirma.createNode(xmlDoc, "dv",   datoUsuario.getDv()));  
                visadoE.appendChild(UtilitariosFirma.createNode(xmlDoc, "idPerfil",   ""+datoUsuario.getIdPerfil()));  
                visadoE.appendChild(UtilitariosFirma.createNode(xmlDoc, "perfil",   datoUsuario.getPerfilSTR()));  
                visadoE.appendChild(UtilitariosFirma.createNode(xmlDoc, "fecha",   sdf.format(new Date())));  
                //util.outputDocToFile(xmlDoc, signatureFile);
                signingResult.xmlDocSigned = xmlDoc;
            }else{
                //System.out.println("NO VISADO...");    
                throw new DocumentSignException("");
            }

        } catch (DocumentSignException gsex) {
            String errorMessage = "Documento ya fue visado por usted.";
            throw new DocumentSignException(errorMessage);
        } catch (Exception gsex) {
            String errorMessage = "Falló proceso de Visado.\n" +
                "Detalle del Problema: " + gsex.getMessage();
            throw new DocumentSignException(errorMessage, gsex);
        }
        return signingResult;
    }    
    





    static class CertificationChainAndSignatureBase64 {
        public String mCertificationChain = null;
        public String mSignature = null;
        public org.w3c.dom.Document xmlDocSigned = null;
    }
    
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

    private String getEncodedPassword(String clearTextPassword)throws NoSuchAlgorithmException
    {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(clearTextPassword.getBytes());
      byte[] result = md.digest();
      StringBuffer sb = new StringBuffer();
      for(int i=0; i<result.length; i++){
          String s = Integer.toHexString(result[i]);
          int length = s.length();
          if(length >= 2)
              sb.append(s.substring(length-2, length));
          else {
              sb.append("0");
              sb.append(s);
          }
      }
      return sb.toString();
    }

    public boolean existeVisadoPrevio(String firmante, String idPerfil, NodeList visadosNL){
        boolean resp = false;
        VisadoAntVO usuarioAnt = new VisadoAntVO();
        ArrayList listado = new ArrayList();
        for (int i = 0; i < visadosNL.getLength(); i++)
        {
            NodeList nlSigner = ((Node)visadosNL.item(i)).getChildNodes();//lista signer
            for (int j = 0; j < nlSigner.getLength(); j++) //por cada signer
            {
                if (((Node)nlSigner.item(j)).getNodeType() == Node.ELEMENT_NODE)
                {
                    NodeList nlDatos = ((Element)nlSigner.item(j)).getChildNodes(); //lista con nombre y rut
                    usuarioAnt = new VisadoAntVO();
                    for (int k = 0; k < nlDatos.getLength(); k++) //por cada nombre y/o rut
                    {
                        if (nlDatos.item(k).getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element dato = (Element)nlDatos.item(k);
                            if (dato.getNodeName().equals("rut"))
                            {
                                NodeList nlElemento = dato.getChildNodes(); //data (valor en si)
                                for (int l = 0; l < nlElemento.getLength(); l++){
                                    //usuarioAnt.setRut(Integer.parseInt(((Node)nlElemento.item(l)).getNodeValue()));
                                    usuarioAnt.setRut(((Node)nlElemento.item(l)).getNodeValue());
                                }
                            }
                            if (dato.getNodeName().equals("idPerfil"))
                            {
                                NodeList nlElemento = dato.getChildNodes(); //data (valor en si)
                                for (int l = 0; l < nlElemento.getLength(); l++){
                                    //usuarioAnt.setIdPerfil(Integer.parseInt(((Node)nlElemento.item(l)).getNodeValue()));
                                    usuarioAnt.setIdPerfil(((Node)nlElemento.item(l)).getNodeValue());
                                    listado.add(usuarioAnt);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (int u=0; u< listado.size(); u++){
            usuarioAnt = new VisadoAntVO();
            usuarioAnt = (VisadoAntVO)listado.get(u);
            if(firmante.equals(usuarioAnt.getRut()) && idPerfil.equals(usuarioAnt.getIdPerfil())){
                resp = true;
                break;
            }
        }
        return resp;
    }

}
