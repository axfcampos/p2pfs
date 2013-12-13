package pt.ulisboa.tecnico.p2pfs.fuse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.util.FuseFilesystemAdapterAssumeImplemented;
import net.tomp2p.storage.Data;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaDto;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaEntryDto;
import pt.ulisboa.tecnico.p2pfs.communication.FuseKademliaFileDto;
import pt.ulisboa.tecnico.p2pfs.kademlia.Kademlia;

public class P2PFilesystem extends FuseFilesystemAdapterAssumeImplemented {

	private static Kademlia kademlia;
	private long kademliaId;
	private MemoryDirectory rootDirectory = new MemoryDirectory();

	public P2PFilesystem(long id) {
		// Sprinkle some files around

		this.kademliaId = id;
		this.kademlia = new Kademlia(id, this);


	}



	private List<MemoryPath> memoryPathFromFuseKadmliaDto(FuseKademliaDto dto, MemoryDirectory parent) {

		List<MemoryPath> dir = new ArrayList<MemoryPath>();

		for (FuseKademliaEntryDto entry : dto.getContents())
			if (entry.getType() == FuseKademliaEntryDto.DIR)
				dir.add(new MemoryDirectory(entry.getName()));
			else
				dir.add(new MemoryFile(entry.getName(), parent));

		return dir;

	}

	public MemoryDirectory getRoot(){

		return this.rootDirectory;
	}

	@Override
	public int access(final String path, final int access)
	{

		MemoryPath p = rootDirectory.find(path);


		if(p instanceof MemoryDirectory) {

			MemoryDirectory dir = (MemoryDirectory) p; 

			if (dir.size() != 0)
				return 0;

			try {

				dir.contents = memoryPathFromFuseKadmliaDto(kademlia.getDirectoryObject(path), dir);

				return 0;

			} catch (ClassNotFoundException e) {

				return -ErrorCodes.EEXIST();

			} catch (IOException e) {

				return -ErrorCodes.EEXIST();
			}

		} else if(p instanceof MemoryFile) {

			return 0;

		}

		return 1;
	}

	@Override
	public int create(final String path, final ModeWrapper mode, final FileInfoWrapper info)
	{
		if (getPath(path) != null) {
			return -ErrorCodes.EEXIST();
		}
		final MemoryPath parent = getParentPath(path);
		if (parent instanceof MemoryDirectory) {
			((MemoryDirectory) parent).mkfile(getLastComponent(path));

			if(path.contains("/."))
				return 0;

			try {

				String[] splitter = path.split("/");

				String name = splitter[splitter.length - 1];

				kademlia.createFile(path);
				if(path.substring(0, path.lastIndexOf("/") + 1).equals("/"))
					kademlia.updateDirectory(path.substring(0, path.lastIndexOf('/') + 1)
							, new FuseKademliaEntryDto(name, 'f'));
				else 
					kademlia.updateDirectory(path.substring(0, path.lastIndexOf('/'))
							, new FuseKademliaEntryDto(name, 'f'));

			} catch (IOException e) {

				System.out.println("Problems creating file...");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {

				System.out.println("Problems creating file...");
				e.printStackTrace();
			}

			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int getattr(final String path, final StatWrapper stat)
	{

		if(rootDirectory.find(path) instanceof MemoryFile && !path.contains("/.")) {

			MemoryFile file = (MemoryFile) rootDirectory.find(path);

			if(!file.hasContent())
				try {
					String str = (String) ((FuseKademliaFileDto) kademlia.getFileObject(path)).getContent();

					final byte[] contentBytes = str.getBytes("UTF-8");
					file.setContents(ByteBuffer.wrap(contentBytes));

				} catch (ClassNotFoundException e) {

					System.out.println("Problems openning file...");
				} catch (IOException e) {

					System.out.println("Problems openning file...");
				}


		}

		final MemoryPath p = getPath(path);
		if (p != null) {
			p.getattr(stat);
			return 0;
		}

		return -ErrorCodes.ENOENT();
	}

	private String getLastComponent(String path)
	{
		while (path.substring(path.length() - 1).equals("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if (path.isEmpty()) {
			return "";
		}
		return path.substring(path.lastIndexOf("/") + 1);
	}

	private MemoryPath getParentPath(final String path)
	{
		return rootDirectory.find(path.substring(0, path.lastIndexOf("/")));
	}

	private MemoryPath getPath(final String path)
	{
		return rootDirectory.find(path);
	}

	@Override
	public int mkdir(final String path, final ModeWrapper mode)
	{
		if (getPath(path) != null) {
			return -ErrorCodes.EEXIST();
		}
		final MemoryPath parent = getParentPath(path);
		if (parent instanceof MemoryDirectory) {
			((MemoryDirectory) parent).mkdir(getLastComponent(path));


			String[] splitter = path.split("/");

			String name = splitter[splitter.length - 1];

			try {

				if (path.substring(0, path.lastIndexOf('/') + 1).equals("/"))
					kademlia.updateDirectory(path.substring(0, path.lastIndexOf('/') + 1)
							, new FuseKademliaEntryDto(name, 'd'));
				else
					kademlia.updateDirectory(path.substring(0, path.lastIndexOf('/'))
							, new FuseKademliaEntryDto(name, 'd'));

				kademlia.createDirFile(path);

			} catch (ClassNotFoundException e) {

				System.out.println("Problem creating directory...");
				e.printStackTrace();
			} catch (IOException e) {


				e.printStackTrace();
			}


			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int open(final String path, final FileInfoWrapper info)
	{

		return 0;
	}

	@Override
	public int read(final String path, final ByteBuffer buffer, final long size, final long offset, final FileInfoWrapper info)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(p instanceof MemoryFile)) {
			return -ErrorCodes.EISDIR();
		}
		return ((MemoryFile) p).read(buffer, size, offset);
	}

	@Override
	public int readdir(final String path, final DirectoryFiller filler)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(p instanceof MemoryDirectory)) {
			return -ErrorCodes.ENOTDIR();
		}
		((MemoryDirectory) p).read(filler);
		return 0;
	}

	@Override
	public int rename(final String path, final String newName)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		final MemoryPath newParent = getParentPath(newName);
		if (newParent == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(newParent instanceof MemoryDirectory)) {
			return -ErrorCodes.ENOTDIR();
		}

		if (p instanceof MemoryDirectory) {



		} else if (p instanceof MemoryFile) {

			//kademlia.renameFile();

		}



		p.delete();
		p.rename(newName.substring(newName.lastIndexOf("/")));
		((MemoryDirectory) newParent).add(p);
		return 0;
	}

	@Override
	public int rmdir(final String path)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(p instanceof MemoryDirectory)) {
			return -ErrorCodes.ENOTDIR();
		}
		p.delete();

		try {

			kademlia.removeDir(path);

		} catch (ClassNotFoundException e) {

			System.out.println("Problem removing directory...");
			e.printStackTrace();
		} catch (IOException e) {

			System.out.println("Problem removing directory...");
			e.printStackTrace();
		}

		return 0;
	}

	@Override
	public int truncate(final String path, final long offset)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(p instanceof MemoryFile)) {
			return -ErrorCodes.EISDIR();
		}
		((MemoryFile) p).truncate(offset);
		return 0;
	}

	@Override
	public int unlink(final String path)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		p.delete();

		if(path.contains("/."))
			return 0;

		try {
			kademlia.removeFile(path);

		} catch (ClassNotFoundException e) {

			System.out.println("Problem Removing File...");
			e.printStackTrace();
		} catch (IOException e) {

			System.out.println("Problem Removing File...");
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int write(final String path, final ByteBuffer buf, final long bufSize, final long writeOffset,
			final FileInfoWrapper wrapper)
	{
		final MemoryPath p = getPath(path);
		if (p == null) {
			return -ErrorCodes.ENOENT();
		}
		if (!(p instanceof MemoryFile)) {
			return -ErrorCodes.EISDIR();
		}
		return ((MemoryFile) p).write(buf, bufSize, writeOffset);
	}

	@Override
	public int release(final String path, final FileInfoWrapper info)
	{
		if(path.contains("/."))
			return 0;


		String str = new String( ((MemoryFile)rootDirectory.find(path)).
				getContents().array(), Charset.forName("UTF-8") );


		try {

			kademlia.updateFile(path, new FuseKademliaFileDto(1, 1, str));
		} catch (IOException e) {
			System.out.println("Problems Updating File...");
			e.printStackTrace();
		}

		return 0;
	}

	public void myMount(String username, String path) throws IOException, ClassNotFoundException, FuseException{

		kademlia.setUserName(username);
		kademlia.getMetadata();
		rootDirectory.contents = memoryPathFromFuseKadmliaDto((FuseKademliaDto) kademlia.getMyFileData(), rootDirectory);
		this.log(true).mount(path);
	}

	public void removeStats() {

		try{
			rootDirectory.find("/Stats").delete();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Statst n exist \n");

		}
	}


	public static void main(final String... args) throws FuseException, IOException, ClassNotFoundException, InterruptedException {

		//		new P2PFilesystem(args[0]).log(true).mount(args[1]);

		File f = new File("myid.txt");
		long id = 0;
		if(f.exists()){
			try {
				BufferedReader reader = new BufferedReader(new FileReader("myid.txt"));
				String sid = reader.readLine();
				id = Long.parseLong(sid);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else{

			Calendar c = null;
			Random r = new Random(c.getInstance().getTimeInMillis());
			id = r.nextLong();

			if(id  < 0) id *= -1;
			try {
				PrintWriter writer = new PrintWriter("myid.txt");
				writer.write(String.valueOf(id));
				writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		P2PFilesystem p2pfs;
		//main so com id
		if(args.length == 0){
			p2pfs = new P2PFilesystem(id);
			//chamar shell loop ainda por fazer que permite fazer mount e unmount e getstats e afins
			p2pfs.shell_loop();
		}

		//main com id e nome
		if(args.length == 2){
			p2pfs = new P2PFilesystem(id);
			p2pfs.myMount(args[0], args[1]);
			//chamar o mesmo shell loop de cima, mas ele ja vai reconhecer que ta mounted e comeca num estado correcto
			//a ideia para este e comecar todos os nos sem necessitarem de qualquer input, onde podem logo comecar a levar operacoes de fuse (por exemplo de um script bash)
			p2pfs.shell_loop();
		}

		//main com id e nome e bool para puts e get e fazer difuse ao fuse
		if(args.length == 3){
			p2pfs = new P2PFilesystem(id);
			//nao se faz mount
			p2pfs.put_get_shell_loop(); //Pa fazer puts e gets a vontade

		}

		//main com id e nome e boo para puts e gets e fazer difuse ao fuse e correr super teste de trafego com puts e gets
		if(args.length == 4){
			p2pfs = new P2PFilesystem(id);
			//nao se faz mount
			p2pfs.put_get_shell_loop();

			//LANCAR SUPER TESTE QUE CRIA IMENSOS OBJECTOS E FAZ PUTS E GETS A SIMULAR UM FUSE A FUNCIONAR MUITO RAPIDO

		}
	}



	private void shell_loop() throws IOException, FuseException{
		System.out.println("Welcome to the P2PFS shell (FUSE enabled) (type 'help' for list of commands)");
		String input;
		while(!(input = (new BufferedReader(new InputStreamReader(System.in))).readLine() ).equals("quit")){

			if(input.equals("help")){
				System.out.println("Command list: \n help \n mount \'fsname\' \n unmount \n stats \n mystats \n quit \n");
			}else{
				if((input.split(" "))[0].equals("mount") && (input.split(" ")).length == 2){

					//faz mount
					//
					if(this.isMounted()){
						System.out.println("Error: fs already mounted!");
					}else{
						System.out.println("Mounting.... " + (input.split(" "))[1] + " file system.");
						this.mount((input.split(" "))[1]);
					}

				}else{
					if((input.split(" "))[0].equals("unmount") && (input.split(" ")).length == 1){

						//faz unmount 
						if(this.isMounted()){
							this.unmount();
						}else{
							System.out.println("Error: fs already unmounted!");
						}

					}else{
						if((input.split(" "))[0].equals("stats") && (input.split(" ")).length == 1){

							//ir buscar as stats ao gossip
						}else{
							if((input.split(" "))[0].equals("mystats") && (input.split(" ")).length == 1){

								kademlia.getStorageMemory().getAggStatsFiles().toString();
							}else{
								System.out.println("Error: malformed input, type 'help' for commands");
							}}}}}
		}
		System.out.println("bye.");
	}

	private void put_get_shell_loop() throws IOException, ClassNotFoundException, InterruptedException{

		System.out.println("Welcome to the P2PFS shell (FUSE disabled) (type 'help' for list of commands)");
		String input;
		while(!(input = (new BufferedReader(new InputStreamReader(System.in))).readLine() ).equals("quit")){

			if(input.equals("help")){
				System.out.println("Command list: \n help \n put \'key\' \'value\' \n get \'key\' \n quit \n");
			}else{
				if((input.split(" "))[0].equals("put") && (input.split(" ")).length == 3){

					//faz put
					this.kademlia.store(input.split(" ")[1], new FuseKademliaFileDto(1, 1, input.split(" ")[2]));

				}else{
					if((input.split(" "))[0].equals("get") && (input.split(" ")).length == 2){

						//faz get
						System.out.println(this.kademlia.get(input.split(" ")[1]).toString());
					}else{
						if((input.split(" "))[0].equals("rm") && (input.split(" ")).length == 2){

							//faz remove
							this.kademlia.remove(input.split(" ")[1]);

						}else{
							if((input.split(" "))[0].equals("t2") && (input.split(" ")).length == 1){

									//faz remove
									this.test2();

								}else{
									
							System.out.println("Error: malformed input, type 'help' for commands");
							}
						}}}}
		}

		System.out.println("bye.");

	}


	private void test1() throws IOException, ClassNotFoundException{


		ArrayList<Integer> vect = null;
		int numbfiles =30;

		for(int a = 1 ; a<numbfiles; a++){	
			vect.add(a);
		}

		//cria Ficheiros
		FuseKademliaFileDto fkDTO= new FuseKademliaFileDto(1, 1 ,"Isto é o conteudo do ficheiro......Isto é o conteudo do ficheiro......");
		for(int v=0;v < vect.size();v++){
			kademlia.store("file"+vect.get(v), fkDTO);
		}

		//puts e gets avontade
		for(int x=0; x<10;x++){

			for(int z=0; z<20;z++){
				Random r= new Random();
				int i =r.nextInt();

				if(i < 0)
				{	
					//put de um ficheiro aleatorio
					i = i*(-1);
					i=i%numbfiles;
					kademlia.store("file"+vect.get(i) , fkDTO);					
				}else{

					//get de file aleatorio

					i=i%numbfiles;
					kademlia.get("file"+i);

				}
			}

		}

	} 



	private void test2() throws IOException,ClassNotFoundException, InterruptedException{


		FuseKademliaFileDto fkDTO= new FuseKademliaFileDto(1, 1 ,"Isto é o conteudo do ficheiro......Isto é o conteudo do ficheiro......" +
				"Isto é o conteudo do ficheiro......Isto é o conteudo do ficheiro......Isto é o conteudo do ficheiro......" +
				"Isto é o conteudo do ficheiro......Isto é o conteudo do ficheiro......Isto é o conteudo do ficheiro......" +
				"Isto é o conteudo do ficheiro......Isto é o conteudo do ficheiro......Isto é o conteudo do ficheiro......" +
				"Isto é o conteudo do ficheiro......Isto é o conteudo do ficheiro......Isto é o conteudo do ficheiro......");
		
		Writer writer = null;
		///////////	Date date 
		Date date = null ;
		long dif1 = 0;
		long difFromStart = 0;
		
		Calendar calendar = Calendar.getInstance();
		Date start = calendar.getTime();

		while(true){

			calendar = Calendar.getInstance();
			Date s = calendar.getTime();
//			System.out.println("Time"+ s);
//25 Puts
			kademlia.store("file1", fkDTO);
			kademlia.store("file2", fkDTO);
			kademlia.store("file3", fkDTO);
			kademlia.store("file4", fkDTO);
			kademlia.store("file5", fkDTO);
			
			kademlia.store("file6", fkDTO);
			kademlia.store("file7", fkDTO);
			kademlia.store("file8", fkDTO);
			kademlia.store("file9", fkDTO);
			kademlia.store("file10", fkDTO);
			
			kademlia.store("file11", fkDTO);
			kademlia.store("file12", fkDTO);
			kademlia.store("file13", fkDTO);
			kademlia.store("file14", fkDTO);
			kademlia.store("file15", fkDTO);
			
			kademlia.store("file1", fkDTO);
			kademlia.store("file2", fkDTO);
			kademlia.store("file3", fkDTO);
			kademlia.store("file4", fkDTO);
			kademlia.store("file5", fkDTO);
			
			kademlia.store("file6", fkDTO);
			kademlia.store("file7", fkDTO);
			kademlia.store("file8", fkDTO);
			kademlia.store("file9", fkDTO);
			kademlia.store("file10", fkDTO);

//25 GETS			
			kademlia.get("file1");
			kademlia.get("file2");
			kademlia.get("file3");
			kademlia.get("file4");
			kademlia.get("file5");
			
			kademlia.get("file6");
			kademlia.get("file7");
			kademlia.get("file8");
			kademlia.get("file9");
			kademlia.get("file10");

			kademlia.get("file11");
			kademlia.get("file12");
			kademlia.get("file13");
			kademlia.get("file14");
			kademlia.get("file15");
			
			kademlia.get("file1");
			kademlia.get("file2");
			kademlia.get("file3");
			kademlia.get("file4");
			kademlia.get("file5");
			
			kademlia.get("file6");
			kademlia.get("file7");
			kademlia.get("file8");
			kademlia.get("file9");
			kademlia.get("file10");

			calendar = Calendar.getInstance();
			Date a = calendar.getTime();
//			System.out.println("Time"+ a);
			dif1 = a.getTime() - s.getTime();
			date = calendar.getTime();
			
			
			difFromStart = s.getTime()- start.getTime() ;
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("results.txt", true)));
//		    out.println("---------------------------------------");
		    out.println(" Intervalo de tempo::" + dif1 + ":tempo para o inicio::" + difFromStart );
		    out.close();
			
			
		//	writer = new BufferedWriter(new OutputStreamWriter(	new FileOutputStream("results.txt"), "utf-8"));
			
//			writer.append("---------------------------------------\n");
//			writer.append("Intervalo de tempo:: " + dif1 + ":::Data:::" + date + "\n");
//			//writer.write("Intervalo de tempo:: " + dif1 + ":::Data:::" + date + "\n");
//			writer.close();
			// report

		}	
		

	} 


}
