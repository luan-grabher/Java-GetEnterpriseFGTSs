package Modelo;
import Controle.Controlador;
import java.awt.Desktop;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Calendar;
import java.util.List;
import main.Arquivo;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.wait.wait;

public class Navegador {
    
    /* INFORMAÇÕES PARA IMPRESSÃO */
    public String pasta_empresa = "";
    public String cnpj = "";
    public String observacao = "";
    public String validade = "";
    public String[] opcoes_status = {"DESABILITADO","ERRO","HÁ PENDÊNCIAS","SEM PENDÊNCIAS"};
    public static String texto_csv_log = "";
    
    /* CONFIGURAÇÕES DRIVER */
    private static WebDriver driver = null;
    private WebElement e = null;
    private List<WebElement> es = null;
    private static JavascriptExecutor js;
    
    /* VALORES AUXILIARES */
    private final int ano = Calendar.getInstance().get(Calendar.YEAR);
    
    /* PASTAS */
    private static final String pasta_downloads = "C:\\Users\\" + System.getProperty("user.name") + "\\Downloads";
    private static final String pasta_desktop = "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop";
    public static long nro_Downloads = 0;
    
    /* CONTAGEM DE LINHAS */
    public static long linha = 0;
    
    /*FUNÇÕES DE CONTROLE*/
    public boolean Iniciar(){
        boolean r = true;
        if (!cnpj.isEmpty() & !pasta_empresa.isEmpty()){
            cnpj = trata_numero(cnpj);
            cnpj = cnpj.length() == 13? "0" + cnpj:cnpj; /* COLOCA ZERO NA FRENTE SE NAO TIVER */
            if (Controlador.isNumeric(cnpj)){
                exibe_acesso();
                Navegar();
                System.out.println("Observação: " + observacao + "\n");
                grava_log();
            }else{
                System.out.println("Linha " + linha + " ignorada.");
            }
        }else{
            System.out.println("Linha " + linha + " ignorada.");
        }
        return r;
    }
    private  void Navegar(){
        try {
            acessar_e_baixar_certificado();
        } catch (Exception e) {}
    }
    
    /* RETORNOS DE LOGS */
    private void grava_log(){
            texto_csv_log += "".equals(texto_csv_log)?"":"\n";
            texto_csv_log += pasta_empresa + ";";
            texto_csv_log += cnpj + ";";
            texto_csv_log += validade + ";";
            texto_csv_log += observacao;
    }
    public static void salvar_log_csv(){
        texto_csv_log = "EMPRESA;CNPJ;VALIDADE;OBSERVAÇÃO\n" + texto_csv_log;
        try{
            String local_csv = pasta_desktop + "\\Certidoes FGTS.csv";
            Arquivo s = new Arquivo();
            boolean save = s.salvar(local_csv, texto_csv_log);
            if(save == false){
                System.out.println("Enfrentei um erro ao salvar o arquivo CSV no DESKTOP (" + local_csv + "). \n");
            }
        }catch(Exception e){
            System.out.println("Enfrentei um erro ao salvar o CSV no DESKTOP: " + e + "\n");
        }
    }
    private void exibe_acesso(){
        System.out.println("\n");
        System.out.println("-----------------------------------------------");
        System.out.println("EMPRESA: " + pasta_empresa);
        System.out.println("CNPJ: " + cnpj);
        System.out.println("_____________INFOS:______________");
    }
    
    /*FUNÇÕES DO DRIVER*/
    public static void abre_navegador(){
        try{
            System.setProperty("webdriver.chrome.driver", "\\\\192.168.0.175\\docs\\Informatica\\Programas\\chromedriver.exe");
            
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--disable-print-preview");
            options.addArguments("--host-resolver-rules=MAP www.piwikrj.caixa.gov.br 127.0.0.1");
            driver = new ChromeDriver(options);
            /*driver.manage().window().fullscreen();*/
            
            if (driver instanceof JavascriptExecutor) {
                js = (JavascriptExecutor)driver;
            }
        }catch(Exception e){
            System.out.println("Ocorreu um erro: \n " + e);
        }
    }
    public static void fecha_navegador(){
        try{
            driver.quit();
        }catch(Exception e){
            System.out.println("Ocorreu um erro: \n " + e);
        }
    }
    public static boolean driver_aberto(){
        boolean b = false;
        try{
            String name_page = driver.getWindowHandle();
            b = true;
        }catch(Exception e){}
        return b;
    }
    
    
    /*NAVEGAÇÃO*/
    private void acessar_e_baixar_certificado(){
        try{
            driver.manage().deleteAllCookies();
            driver.get("https://consulta-crf.caixa.gov.br/Cidadao/Crf/FgeCfSCriteriosPesquisa.asp");
            
            js.executeScript(script_set_cnpj());
            
            e = wait.element(driver, By.cssSelector("a[href=\"javascript:Redirect('FgeCfSImprimirCrf.asp')\"]"));
            
            if (e != null){
                e.click();
                
                e = wait.element(driver, By.cssSelector("a[href=\"javascript:Imprime();\"]"));
                if(e != null){
                    e.click();
                    
                    e = wait.element(driver, By.cssSelector("a[href=\"javascript:window.print();\"]"));
                    if(e != null){
                        if(salvar_certidao("FGTS " + pasta_empresa + " - " + cnpj)){
                            observacao += "Certificado salvo em Downloads.";
                        }else{
                            exibir_erro("Erro ao salvar impressão.");
                        }
                    }else{
                        exibir_erro("Problema ao salvar impressão.");
                    }
                }else{
                    exibir_erro("Problema ao gerar impressão.");
                }
            }else{
                exibir_erro("Problema no CNPJ.");
            }
        }catch(Exception e){
            exibir_erro("Erro na navegação não identificado.", e);
        }
    }
    
    
    /* UTILITARIOS NAVEGAÇÃO */
    private boolean salvar_certidao(String nome_arquivo){
        boolean b = false;
        String script_altera_src_imagem = "document.querySelector(\"td > img\").setAttribute(\"src\","
                                                        + "\"https://lh5.googleusercontent.com/Yh0q2CuDHEzT8P4YJbpViQsM0TqIKcycTWZ8u1imcybyUpXJ52YwO3sXzF-mv-L9NL1c4OeYlV30CFh1o0Fk=w1366-h576\");"
                                       + " document.body.appendChild(document.createElement(\"banana\"));";
        js.executeScript(script_altera_src_imagem);
        
        e = wait.element(driver, By.cssSelector("banana"));
        
        if(e != null){
            if(gravar_validade()){
                e = driver.findElement(By.cssSelector("body > form > table table"));

                String html_certidao = e.getAttribute("innerHTML");
                Arquivo a = new Arquivo();
                b = a.salvar(pasta_downloads + "\\" + nome_arquivo + ".html", html_certidao);
            }else{
                exibir_erro("Erro no html da validade.");
            }
        }
        
        return b;
    }
    private boolean gravar_validade(){
        boolean b = false;
        
        es = driver.findElements(By.cssSelector("font"));
        
        for (int i = 0; i < es.size(); i++) {
            WebElement get = es.get(i);
            String innerHTML = get.getAttribute("innerHTML");
            if(innerHTML.contains("<strong>Validade: </strong>")){
                innerHTML = innerHTML.replaceAll("<strong>Validade: </strong>", "");
                validade = innerHTML;
                b = true;
                break;
            }
        }
        
        return b;
    }
    private boolean salvar_pagina_html(String nome_arquivo){
        atualiza_nro_downloads("html");
        boolean b = false;
            try{
                Robot r = new Robot();

                try{
                    r.keyPress(KeyEvent.VK_CONTROL);
                    r.keyPress(KeyEvent.VK_S);
                    r.keyRelease(KeyEvent.VK_S);
                    r.keyRelease(KeyEvent.VK_CONTROL);

                    r.delay(1000);
                    
                    
                    String nome_temporario = "TEMP HTML " +  randInt(10000, 99999);
                    send_keys(nome_temporario);

                    r.keyPress(KeyEvent.VK_ENTER);
                    r.keyRelease(KeyEvent.VK_ENTER);
                    
                    if(espera_Download("html")){
                        File file_temporario = new File(pasta_downloads + "\\" + nome_temporario + ".html");
                        File novo_nome =  new File(pasta_downloads + "\\" + nome_arquivo + ".html");
                        if(novo_nome.exists() == true){novo_nome.delete();}
                        file_temporario.renameTo(novo_nome);

                       esperar(2);
                        if(novo_nome.exists()){
                            b = true;
                        }else{
                            observacao +=  "Não consegui salvar o arquivo: " + novo_nome.getAbsolutePath() + ". ";
                            System.out.println("Não consegui salvar o arquivo: " + novo_nome.getAbsolutePath());
                        }
                    }else{
                        b = false;
                    }
                }catch(Exception e){System.out.println("Erro ao pressionar teclas para impressão: " + e);}     
            }catch(Exception e){System.out.println("Erro ao printar página!");}
        return b;
    }
    private String script_set_cnpj(){
        return  "ins = \"" + cnpj + "\";\n" +
                        "\n" +
                        "document.querySelector(\"select.formdir option[selected]\").removeAttribute(\"selected\");\n" +
                        "document.querySelector(\"select.formdir option[value='1']\").setAttribute(\"selected\",\"\");\n" +
                        "\n" +
                        "LiberarInscricao();\n" +
                        "document.frmPortal.ImportWorkEmpregadorCodigoInscricaoAlfanum.value = ins;\n" +
                        "\n" +
                        "tpinscr = 1;\n" +
                        "if (FgeEmValidaAceitaEmpregador(tpinscr,ins,\"\",\"\")) {\n" +
                        "	if (FgeEmValidaCGC(ins,true)){\n" +
                        "		\n" +
                        "		codigo1 = String.fromCharCode((document.all.resultadopath.value.slice(1,4) *1)+1);\n" +
                        "		codigo2 = String.fromCharCode((document.all.resultadopath2.value.slice(1,4) *1)+1);\n" +
                        "		codigo3 = String.fromCharCode((document.all.resultadopath3.value.slice(1,4) *1)+1);\n" +
                        "		codigo4 = String.fromCharCode((document.all.resultadopath4.value.slice(1,4) *1)+1);\n" +
                        "		codigo5 = String.fromCharCode((document.all.resultadopath5.value.slice(1,4) *1)+1);\n" +
                        "\n" +
                        "		codigo_captcha = codigo1 + codigo2 + codigo3 + codigo4 + codigo5;\n" +
                        "		/*alert(\"O Captcha é: \" + codigo_captcha);*/ \n" +
                        "		\n" +
                        "		\n" +
                        "		top.frames[1].document.getElementById(\"txtCaptchaVerificar\").value = codigo_captcha ;\n" +
                        "		top.frames[1].document.getElementById(\"checkIt\").submit();\n" +
                        "	}\n" +
                        "}";
        
    }
    
    
    /*ARQUIVOS*/
    private boolean espera_Download(String extensao){
        boolean b =  false;
        for (int i = 0; i < 23; i++) {
            long nro_now = get_nro_downloads(extensao);
            if(nro_Downloads != nro_now){
                b = true;
                break;
            }
            esperar(1);
        }
        return b;
    }
    private static File pega_ultimo_arquivo_da_pasta(String location, String extensao) {
        int latestDate = -1;
        File[] files = null;
        try{
            File dir = new File(location);
            files = dir.listFiles();

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if(file.getName().contains("." + extensao)){
                    if(latestDate != -1){
                        if (file.lastModified() > files[latestDate].lastModified()){
                            latestDate = i;
                        }
                    }else{
                        latestDate = i;
                    }
                } 
            }
        }catch(Exception e){}
        
        if(latestDate == -1){
            return null;
        }else{
            return files[latestDate];
        }
    }
    private void imprimir_arquivo(File arquivo){
        try{
            Desktop.getDesktop().print(arquivo);
            esperar(5);
        }catch(Exception e){
            observacao += "Não Imprimiu o ARQUIVO (" + arquivo.getAbsolutePath() + "): " + e + ". ";
            System.out.println("Não Imprimiu o ARQUIVO (" + arquivo.getAbsolutePath() + "): " + e );
        }  
    }
    
    private void atualiza_nro_downloads(String extensao){
        nro_Downloads = get_nro_downloads(extensao);
    }
    private long get_nro_downloads(String extensao){
        long nro = 0;
        try{
            File dir = new File(pasta_downloads);
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if(file.getName().contains("." + extensao)){nro++;}
            }
        }catch(Exception ex){}

        return nro;
    }
    
    /*TRATAMENTOS*/
    private String trata_numero(String str){
        str = str.replaceAll("\\.", "");
        str = str.replaceAll("-", "");
        str = str.replaceAll("/", "");
        str = str.replaceAll("\r", "");
        return str;
    }
    
    /*UTILITARIOS*/
    private void exibir_erro(String mensagem, Exception e){
        observacao += mensagem;
        System.out.println(mensagem + ": " +  e);
    }
    private void exibir_erro(String mensagem){
        observacao += mensagem;
        System.out.println(mensagem);
    }
    private void esperar(long segundos){
        wait.java(segundos);
    }
    private void send_keys(String text){
        try{
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);

            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        }catch(Exception e){}
    }
    public static int randInt(int Min, int Max) {
        int randomNum = Min + (int)(Math.random() * ((Max - Min) + 1));
        return randomNum;
    }
}
