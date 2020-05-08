package Controle;

import Modelo.Navegador;
import Visao.carregando;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import main.Arquivo;

public class Controlador {
    public static String mensagem_final  = "";
    public static String arquivo = "";
    
    public static void iniciar(){
        if (verificaArquivo()) {
            
            try{
                //exibe carregamento
                JFrame carregamento = new carregando();
                carregamento.setVisible(true);
                
                percorre_arquivo();
                mensagem_final = "Certidões Emitidas! Verifique as pastas das empresas!";

                //esconde carregamento
                carregamento.setVisible(false);
        
                
                JOptionPane.showMessageDialog( null ,
                        mensagem_final,
                        " Programa Finalizado!" , JOptionPane.INFORMATION_MESSAGE );
                //System.exit(0);
            }catch(Exception e){
                JOptionPane.showMessageDialog( null ,
                    " OCORREU UM ERRO NÃO ESPERADO NO JAVA! ERRO: \n",
                    " ERRO NO JAVA!" , JOptionPane.WARNING_MESSAGE );
            }
        }
    }   
    public static boolean verificaArquivo(){
        boolean retorno = false;
        String local = arquivo;
        if (local.length() <= 5) {
            System.out.println("O arquivo está em branco!");
            JOptionPane.showMessageDialog( null ,
                    " O local do arquivo não pode ficar em branco!" ,
                    " Alerta" , JOptionPane.WARNING_MESSAGE );
        }else if (".csv".equals(local.substring(local.length()-4, local.length()))){
            //VERIFICAR SE ARQUIVO EXISTE
            File arq = new File(local);
            
            if (arq.exists() == false) {
                System.out.println("O arquivo não existe!");
                JOptionPane.showMessageDialog( null ,
                    " O arquivo não existe!" ,
                    " Alerta" , JOptionPane.WARNING_MESSAGE );
            }else {
                retorno = true;
            }
        }else{
            System.out.println("O arquivo selecionado não é CSV!");
            JOptionPane.showMessageDialog( null ,
                    " Deve ser um arquivo CSV!" ,
                    " Alerta" , JOptionPane.WARNING_MESSAGE );
        }
        
        return retorno;
    }
    public static boolean percorre_arquivo(){
        boolean r  = true;
        String textoArquivo = "";
        
        try{
            Arquivo leitor = new Arquivo();
            textoArquivo = leitor.ler(arquivo);
        }catch(Exception e){
            System.out.println("Enfrentei um erro ao ler o arquivo: " + e + "\n");
        }
        String[] linhas = textoArquivo.split("\n");
        
        Navegador.abre_navegador();
        
        long executados  = 0;
        for (String linha : linhas) {
            executados++;
            if (Navegador.driver_aberto() == false){break;}
            String[] coluna = linha.split(";");
            //Define novo Navegador
            Navegador chrome = new Navegador();
            Navegador.linha =  executados;
            //Define variaveis
            chrome.pasta_empresa =  coluna[0];
            chrome.cnpj = coluna[1];
            chrome.Iniciar();
        }
        
        Navegador.fecha_navegador();
        
        
        if(executados == 0){
            System.out.println("Nenhum CNPJ executado!");
        }else{
            Navegador.salvar_log_csv();
        }
        
        return r;
    }
    public static boolean isNumeric (String s) {
        try {
            Long.parseLong (s); 
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
