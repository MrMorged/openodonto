import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import br.ueg.openodonto.controle.ManterPaciente;
import br.ueg.openodonto.controle.context.ApplicationContext;
import br.ueg.openodonto.dominio.Paciente;
import br.ueg.openodonto.dominio.Telefone;
import br.ueg.openodonto.dominio.Usuario;
import br.ueg.openodonto.dominio.constante.TiposTelefone;
import br.ueg.openodonto.dominio.constante.TiposUF;
import br.ueg.openodonto.visao.ApplicationView;


public class Main {

	private static char[] CONSOANTES = {'b','c','d','f','g','h','j','k','l','m','n','p','q','r','s','t','v','x','y','z','w'};
	private static char[] VOGAIS = {'a','e','i','o','u'};
	private static char[] NUMBERS = {'0','1','2','3','4','5','6','7','8','9'};
	private static int MaxEstados = TiposUF.values().length;
	private static int MaxTiposTel = TiposTelefone.values().length;
	
	static volatile int createTimes;
	static volatile int recuperarTimes;
	static volatile int updateTimes;
	static volatile int deleteTimes;
	private static int genTimes = 10; // Um milh�o de vezes
	private static int users    = 1;  // Quantidade de usu�rios Simulados
	
	
	static volatile long timeCreate;
	static volatile long timeRecuperar;
	static volatile long timeUpdate;
	static volatile long timeDelete;
	static volatile long timeGenerateData;
	static volatile boolean isPrinted    = false;  // Quantidade de usu�rios Simulados
	
	private ManterPaciente manterPaciente;
	private UnitTestContext context;
	
	
	public Main() {
		manterPaciente = new ManterPaciente(){
			public void makeView(Properties params){
				this.setView(new UnitTestView());
			}
		};
		manterPaciente.setContext(context = new UnitTestContext());
	}
	
	public static void main(String[] args) {
		Queue<Paciente> jobData = new ConcurrentLinkedQueue<Paciente>();
		long timeGenerateDataStart = System.currentTimeMillis();
		for(int i = 0 ; i < genTimes ; i++){
			jobData.add(generatePaciente());
		}
		timeGenerateData = System.currentTimeMillis() - timeGenerateDataStart;
		Job job = new Job(jobData);
		Executor pool = Executors.newCachedThreadPool();
		List<Runnable> bootUsers = new ArrayList<Runnable>();
		for(int i = 0 ; i < users ; i++){
			bootUsers.add(new Stress(job));
		}
		Iterator<Runnable> usersIterator = bootUsers.iterator();
		while(usersIterator.hasNext()){
			pool.execute(usersIterator.next());
		}		
	}
	
	
	
	public static void printResults(){
		System.out.format("%-30s", "Opera��o");
		System.out.format("%-30s", "Tempo");
		System.out.format("%-30s", "Execu��es").append("\n");
		
		System.out.format("%-30s", "Cria��o Objetos");
		System.out.format("%-30d", timeGenerateData);
		System.out.format("%-30d", genTimes).append("\n");
		
		System.out.format("%-30s", "Inser��o");
		System.out.format("%-30d", timeCreate);
		System.out.format("%-30d", createTimes).append("\n");
		
		System.out.format("%-30s", "Recupera��o");
		System.out.format("%-30d", timeRecuperar);
		System.out.format("%-30d", recuperarTimes).append("\n");
		
		System.out.format("%-30s", "Atualiza��o");
		System.out.format("%-30d", timeUpdate);
		System.out.format("%-30d", updateTimes).append("\n");
		
		System.out.format("%-30s", "Remo��o");
		System.out.format("%-30d", timeDelete);
		System.out.format("%-30d", deleteTimes).append("\n");
		
		System.out.format("%-30s", "Total Trans. BD");
		System.out.format("%-30d", timeCreate+timeRecuperar+timeUpdate+timeDelete);
		System.out.format("%-30d", createTimes+recuperarTimes+updateTimes+deleteTimes).append("\n");
		
		
	}
	
	private static Paciente generatePaciente(){
		Paciente paciente = new Paciente();
		paciente.setCidade(generateWord(5,10, 1,CONSOANTES,VOGAIS));
		paciente.setCpf(generateWord(11,11, 1,NUMBERS));
		paciente.setDataInicioTratamento(new Date(System.currentTimeMillis()));
		paciente.setDataRetorno(new Date(System.currentTimeMillis()));
		paciente.setDataTerminoTratamento(new Date(System.currentTimeMillis()));
		paciente.setEmail(generateWord(5,10, 1,CONSOANTES,VOGAIS) + "@" + generateWord(5,10, 1,CONSOANTES,VOGAIS) + ".com");
		paciente.setEndereco(generateWord(5,10, 1,CONSOANTES,VOGAIS) + " N " + generateWord(2,5, 1,NUMBERS));
		paciente.setEstado(TiposUF.values()[generateNumber(MaxEstados)]);
		paciente.setNome(generateWord(5,15,4,CONSOANTES,VOGAIS));
		paciente.setObservacao(generateWord(5,10,50,CONSOANTES,VOGAIS));
		paciente.setReferencia(generateWord(5,15,4,CONSOANTES,VOGAIS));
		paciente.setResponsavel(generateWord(5,15,4,CONSOANTES,VOGAIS));
		paciente.setTelefone(generateTelefones());
		return paciente;
	}
	
	private static List<Telefone> generateTelefones(){
		int qTels = generateNumber(10);
		List<Telefone> telefones = new ArrayList<Telefone>();
		for(int i = 0;  i < qTels ; i++){
			Telefone e = new Telefone();
			e.setNumero(generateWord(10,15, 1,NUMBERS));
			e.setTipoTelefone(TiposTelefone.values()[generateNumber(MaxTiposTel)]);
			telefones.add(e);
		}
		return telefones;
	}
	
	public static String generateWord(int min,int max,int words,char[]... domain){
		StringBuilder stb = new StringBuilder();		
		for(int i = 0 ; i < words ; i++){
			int rLen = generateNumber(max);
			rLen = rLen < min ? min : rLen; 
			for (int j = 0; j < rLen; j++) {
				int rDomain = generateNumber(domain.length);
				int rChar = generateNumber(domain[rDomain].length);
				stb.append(domain[rDomain][rChar]);
			}
			if(i < words){
				stb.append(" ");
			}
		}
		return stb.toString();
	}
	
	private static int generateNumber(int max){
		return (int)(Math.random() * 1000) % max;
	}
	
	public void create(){
		manterPaciente.acaoAlterar();
		createTimes++;
	}
	
	public void recuperar(Long id){
		try{
			manterPaciente.setOpcao("codigo");
			manterPaciente.setParamBusca(String.valueOf(id));
			manterPaciente.acaoPesquisar();
			
			manterPaciente.acaoCarregarBean();
		}finally{
		    recuperarTimes++;
		}
	}
	
	public void update(){
		manterPaciente.acaoAlterar();
		updateTimes++;
	}
	
	public void delete(){
		manterPaciente.acaoRemoverSim();
		deleteTimes++;
	}
	
	public ManterPaciente getManterPaciente() {
		return manterPaciente;
	}
	
	public UnitTestContext getContext() {
		return context;
	}
}

class Stress implements Runnable{

	private Job job;
	
	public Stress(Job job){
		this.job = job;
	}
	
	@Override
	public void run() {
		while(true){
			Paciente paciente;
			synchronized (job) {
				if(job.hasData()){
					paciente = job.getPaciente();
				}else{
					if(!Main.isPrinted){
					    Main.printResults();
					    Main.isPrinted = true;
					}
					break;
				}
			}
			doCrud(paciente);
		}
	}
	
	private void doCrud(Paciente paciente){
		long parcial = 0;
		Main main = new Main();	
		main.getManterPaciente().setBackBean(paciente);
		
		
		parcial = System.currentTimeMillis();
		main.create();
		Main.timeCreate += System.currentTimeMillis() - parcial;
		
		parcial = System.currentTimeMillis();
		main.getContext().getValues().put("index", 0);
		main.recuperar(paciente.getCodigo());
		Main.timeRecuperar += System.currentTimeMillis() - parcial;
		
		parcial = System.currentTimeMillis();			
		main.update();
		Main.timeUpdate += System.currentTimeMillis() - parcial;
		
		parcial = System.currentTimeMillis();
		main.delete();
		Main.timeDelete += System.currentTimeMillis() - parcial;			
	}
	
}


class UnitTestContext implements ApplicationContext{

	private Map<String , Object> values;
	
	public UnitTestContext() {
		values = new HashMap<String, Object>();
	}
	
	public Map<String, Object> getValues() {
		return values;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name, Class<T> classe) {
		Object value = values.get(name);
		if(value != null){
			return (T)value;
		}
		return null;
	}

	@Override
	public String getParameter(String name) {
		Object value = values.get(name);
		if(value != null){
			return String.valueOf(value);
		}
		return null;
	}

	@Override
	public Usuario getUsuarioSessao() {
		return null;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return values;
	}

	@Override
	public void removeAttribute(String name) {
		values.remove(name);
	}

	@Override
	public void addAttribute(String name, Object value) {
		values.put(name, value);
	}
	
}


class UnitTestView implements ApplicationView {

	@Override
	public void addLocalDynamicMenssage(String msg, String target,
			boolean targetParam) {
		
	}

	@Override
	public void addLocalMessage(String key, String target, boolean targetParam) {
		
	}

	@Override
	public void addResourceDynamicMenssage(String msg, String target) {
		
	}

	@Override
	public void addResourceMessage(String key, String target) {
	
	}

	@Override
	public boolean getDisplayMessages() {
		return false;
	}

	@Override
	public boolean getDisplayPopUp() {
		return false;
	}

	@Override
	public Properties getParams() {
		return null;
	}

	@Override
	public String getPopUpMsg() {
		return null;
	}

	@Override
	public void refresh() {
		
	}

	@Override
	public void showAction() {
		
	}

	@Override
	public void showOut() {
		
	}

	@Override
	public void showPopUp(String msg) {
		
	}
	
}

class Job{
	
	private Queue<Paciente> jobData;
	
	public Job(Queue<Paciente> jobData) {
		this.jobData = jobData;
	}
	
	public synchronized boolean hasData(){
		return !jobData.isEmpty();
	}
	
	public synchronized Paciente getPaciente(){
		return jobData.poll();
	}
	
}

