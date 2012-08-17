package org.xreports.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;


/**
 * Classe di generica utilità per la gestione dei files, 
 * in gran parte copiata e adattata da commons-io 1.4 .
 * 
 * @author pier
 * 
 */
public class FileUtil { 
  private static FileUtil   m_staticUtil        = null;

  public static final int   PATH                = 0;
  public static final int   NOME                = 1;
  public static final int   ESTENSIONE          = 2;
  public static final int   FOS                 = 3;

  public static final int   ARRAY_ELEMENT       = 4;

  public static final int   MAX_TENTATIVI       = 5;

  /**
   * The default buffer size to use.
   */
  private static final int  DEFAULT_BUFFER_SIZE = 1024 * 4;

  private FileUtil() {
  }

  /**
   * Ritorna l'istanza statica
   * 
   * @return Util
   */
  public static synchronized FileUtil getInstance() {
    if (null == m_staticUtil) {
      // Se non c'è, la creo...
      m_staticUtil = new FileUtil();
    }
    return m_staticUtil;
  }

  /**
   * Ritorna il path per arrivare alla directory in cui si trova il jar che si sta eseguendo partendo da una classe
   * 
   * @param classe
   *          Una classe da controllare
   * @return String Il path alla directory che contiene il jar
   * @throws IllegalArgumentException
   */
  public static String getPathToJarfileDir(Class<?> classe) throws IllegalArgumentException {
    if (classe == null)
      throw new IllegalArgumentException("getPathToJarfileDir: Parametro Class = null");
    String url = classe.getResource("/" + classe.getName().replaceAll("\\.", "/") + ".class").toString();
    url = url.substring(4).replaceFirst("/[^/]+\\.jar!.*$", "/");
    try {
      File dir = new File(new URL(url).toURI());
      url = dir.getAbsolutePath();
    } catch (Exception e) {
      url = null;
    }

    return url;
  }

  /**
   * Ritorna il path per arrivare alla directory in cui si trova il jar che si sta eseguendo partendo da un oggetto
   * 
   * @param object
   *          Un oggetto contenuto nel file jar
   * @return String Il path alla directory che contiene il jar
   * @throws IllegalArgumentException
   */
  public static String getPathToJarfileDir(Object object) throws IllegalArgumentException {
    if (object == null)
      throw new IllegalArgumentException("getPathToJarfileDir: Parametro Object = null");
    Class<?> classe = object.getClass();
    String path = getPathToJarfileDir(classe);

    return path;
  }

  /**
   * Ritorna il path per arrivare alla directory in cui si trova il jar che si sta eseguendo partendo da una classe contenuta dentro
   * il jar
   * 
   * @param String
   *          Nome della classe da controllare
   * @return String Il path alla directory che contiene il jar
   */
  public static String getPathToJarfileDir(String nomeClasse) {
    String path = null;
    try {
      Class<?> classe = Class.forName(nomeClasse);
      path = getPathToJarfileDir(classe);
    } catch (Exception e) {
      path = null;
    }

    return path;
  }

  /**
   * Ritorna un booleano che dice se sto eseguendo un jar o meno
   * 
   * @param Class
   *          <?> Una classe qualsiasi
   * @return boolean <b>true</b> se sto eseguendo un jar, <b>false</b> altrimenti
   * @throws IllegalArgumentException
   */
  public static boolean isJarfile(Class<?> classe) throws IllegalArgumentException {
    String path = getPathToJarfileDir(classe);
    if (path == null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Ritorna un booleano che dice se sto eseguendo un jar o meno
   * 
   * @param Object
   *          Un oggetto da controllare
   * @return boolean <b>true</b> se sto eseguendo un jar, <b>false</b> altrimenti
   * @throws IllegalArgumentException
   */
  public static boolean isJarfile(Object object) throws IllegalArgumentException {
    String path = getPathToJarfileDir(object);
    if (path == null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Ritorna un booleano che dice se sto eseguendo un jar o meno
   * 
   * @param String
   *          Nome della classe da controllare
   * @return boolean <b>true</b> se sto eseguendo un jar, <b>false</b> altrimenti
   */
  public static boolean isJarfile(String nomeClasse) {
    String path = getPathToJarfileDir(nomeClasse);
    if (path == null) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Recupera una proprietà da un file di properties
   * 
   * @param String
   *          nomeProprieta - Il nome della proprità da leggere
   * @return String - La proprietà trovata
   */
  public static String recuperaProprieta(String nomeProprieta) throws IllegalArgumentException {
    AppPropertiesBase properties = AppPropertiesBase.getProperties();
    String prop = properties.getProp(nomeProprieta);
    if (prop == null) {
      throw new IllegalArgumentException("Manca la proprietà: " + nomeProprieta);
    }

    return prop;
  }

  /**
   * Spezza il path assoluto del file passato nei suo vari componenti e li ritorna in un array di stringhe. Ad esempio:<br>
   * <tt>splitFilePath("C:/temp/pippo/pluto.txt", false) --> </tt> <tt>{"C:/" , "temp" , "pippo" , "pluto.txt"}</tt> <br/>
   * <tt>splitFilePath("C:/temp/pippo/pluto.txt", true)  --> </tt> <tt>{"C:/" , "temp" , "pippo" , "pluto", "txt"}</tt>
   * 
   * @param file
   *          file di input
   * @param splitExt
   *          se true, divide anche l'estensione dal nome del file
   * @return array con i componenti ordinati del path assoluto, in forma stringa
   * @throws MalformedURLException
   */
  public static String[] splitFilePath(File file, boolean splitExt) {
    try {
      String[] arrayPathNome = new String[2];
      List<String> files = new ArrayList<String>();
      String fileName = file.getName();
      if (splitExt) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
          files.add(fileName.substring(0, i)); //nome file
          files.add(fileName.substring(i + 1)); //estensione file
        } else {
          //non c'è estensione
          files.add(fileName);
        }
      } else {
        //non splitto estensione
        files.add(fileName);
      }

      File dir = file.getParentFile();
      while (dir != null) {
        String name = dir.getName();
        if (name.length() == 0) {
          //probabilmente sono arrivato alla radice
          name = FileUtil.removeTrailingSlash(dir.getPath());
        }
        if (name.length() > 0) {
          files.add(0, name);
        }
        dir = dir.getParentFile();
      }
      return files.toArray(arrayPathNome);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Unisce due path in modo da formare un regolare path. Viene forzato come separatore di directory, il carattere predefinito del
   * file system corrente.
   * 
   * @param path1
   *          primo path
   * @param path2
   *          secondo path
   * @return path composto dall'unione dei due path di input
   * @throws IllegalArgumentException
   *           nel caso uno dei due path sia null
   */
  public static String joinPaths(String path1, String path2) throws IllegalArgumentException {
    return joinPaths(path1, path2, File.separatorChar);
  }

  /**
   * Unisce due path in modo da formare un regolare path. Viene forzato come separatore di directory, il carattere passato come
   * terzo parametro. Esempio:<br>
   * <tt style="margin:8px;">joinPaths("C:\\temp\\", "/pluto/pippo\\test.txt", '/')</tt> <br>
   * ritorna il path <br>
   * <tt style="margin:8px;"> "C:/temp/pluto/pippo/test.txt" </tt>
   * 
   * @param path1
   *          primo path
   * @param path2
   *          secondo path
   * @param separator
   *          carattere di separazione di directory da imporre
   * @return path composto dall'unione dei due path di input
   * @throws IllegalArgumentException
   *           nel caso uno dei due path sia null
   */
  public static String joinPaths(String path1, String path2, char separator) throws IllegalArgumentException {
    String pathOK = removeTrailingSlash(path1) + separator + removeLeadingSlash(path2);
    char oldChar = separator == '/' ? '\\' : '/';
    return pathOK.replace(oldChar, separator);
  }

  /**
   * Wrapper di {@link #endWithSlash(String, char)} con il carattere di separazione del file system corrente.
   * 
   * @param path
   *          path da controllare
   * @return path di input con il carattere finale uguale al separatore di directory di sistema
   * @throws IllegalArgumentException
   *           nel caso path sia null
   */
  public static String endWithSlash(String path) throws IllegalArgumentException {
    return endWithSlash(path, File.separatorChar);
  }

  /**
   * Assicura che il path passato termini con il carattere di separazione indicato. Esempi:
   * 
   * <pre style="margin:0;">
   *   endWithSlash("C:/temp/", '/')   --> "C:/temp/"
   *   endWithSlash("C:/temp", '/')    --> "C:/temp/"
   *   endWithSlash("C:/temp\", '/')   --> "C:/temp/"
   * </pre>
   * 
   * @param path
   *          path da controllare
   * @param separator
   *          carattere separatore di directory
   * @return path di input con il carattere finale uguale al separatore
   * @throws IllegalArgumentException
   *           nel caso path sia null
   */
  public static String endWithSlash(String path, char separator) throws IllegalArgumentException {
    if (path == null) {
      throw new IllegalArgumentException("Il path passato non può essere null!");
    }
    if ( !path.endsWith("/") && !path.endsWith("\\") && !path.endsWith(File.separator)) {
      path = path + separator;
    } else if ( !path.substring(path.length() - 1).equals(String.valueOf(separator))) {
      //caso in cui il carattere finale è un separatore di directory
      //ma non quello che ho indicato io
      path = path.substring(0, path.length() - 1) + separator;
    }
    return path;
  }

  /**
   * Dato un path, rimuove l'eventuale separatore di directory presente alla fine. Esempi:
   * 
   * <pre style="margin:0;">
   *   removeTrailingSlash("C:/temp/")   --> "C:/temp"
   *   removeTrailingSlash("C:\\temp\\") --> "C:\\temp"
   *   removeTrailingSlash("C:/temp")    --> "C:/temp"
   * </pre>
   * 
   * @param path
   *          path di input
   * @return path di input privato dell'eventuale separatore finale di directory.
   * 
   * @throws IllegalArgumentException
   *           nel caso path sia null
   */
  public static String removeTrailingSlash(String path) throws IllegalArgumentException {
    if (path == null) {
      throw new IllegalArgumentException("Il path passato non può essere null!");
    }
    if (path.length() > 0) {
      if (path.endsWith("/") || path.endsWith("\\") || path.endsWith(File.separator)) {
        path = path.substring(0, path.length() - 1);
      }
    }
    return path;
  }

  /**
   * Dato un path, rimuove l'eventuale separatore di directory presente all'inizio. Esempi:
   * 
   * <pre style="margin:0;">
   *   removeLeadingSlash("C:/temp/")   --> "C:/temp/"
   *   removeLeadingSlash("/pippo/pluto.txt") --> "pippo/pluto.txt"
   * </pre>
   */
  public static String removeLeadingSlash(String path) throws IllegalArgumentException {
    if (path == null) {
      throw new IllegalArgumentException("Il path passato non può essere null!");
    }
    if (path.length() > 0) {
      if (path.charAt(0) == '/' || path.charAt(0) == '\\' || path.charAt(0) == File.separatorChar) {
        path = path.substring(1);
      }
    }
    return path;
  }

  /**
   * Crea e apre un file unico. Ci si prova per un massimo di MAX_TENTATIVI
   * 
   * @param path
   *          - path del file da creare
   * @param nomeFile
   *          - nome del file da creare
   * @param estensione
   *          - estensione del file da creare
   * @return Array[File, FOS] - Array con il file ed il fileoutputstream
   * @throws IllegalArgumentException
   */
  public static Object[] createAndOpenUniqueFile(String path, String nomeFile, String estensione) throws IllegalArgumentException {
    Object[] arrayFile = new Object[2];
    FileOutputStream fos = null;
    File file = null;

    int maxTentativi = MAX_TENTATIVI;
    int tentativo = 0;
    /** Controllo che il path e il nome del file siano passati in modo corretto... */
    if (nomeFile == null || nomeFile.equals("")) {
      throw new IllegalArgumentException("Il nome del file non può essere null o vuoto!");
    } else if (nomeFile.endsWith(estensione)) {
      nomeFile = nomeFile.substring(0, nomeFile.length() - estensione.length());
    }
    if (path.endsWith("/") || path.endsWith("\\")) {
      path = path.substring(0, path.length() - 1);
    }

    String nomeFileBase = nomeFile;
    for (tentativo = 0; tentativo < maxTentativi; tentativo++) {
      try {
        //Se riesco a aprire il file in output...
        file = new File(path + "/" + nomeFile + estensione);
        fos = openOutputStream(file);
        break;
      } catch (IOException e) {
        //Non sono riuscito ad aprire un file univoco...
      }

      nomeFile = nomeFileBase + "(" + (tentativo + 1) + ")";
    }

    /**
     * Se abbiamo già fatto 'maxTentativi' lancio eccezione dal momento che non sono riuscito a trovare un file scrivibile...
     */
    if (tentativo == maxTentativi) {
      throw new IllegalArgumentException("Non è stato possibile generare un url per un fle scrivibile! Sono stati superati i " + maxTentativi
          + " tentativi massimi di prova");
    }

    arrayFile[0] = file;
    arrayFile[1] = fos;

    return arrayFile;
  }

  //-----------------------------------------------------------------------
  /**
   * Opens a {@link FileOutputStream} for the specified file, checking and creating the parent directory if it does not exist.
   * <p>
   * At the end of the method either the stream will be successfully opened, or an exception will have been thrown.
   * <p>
   * The parent directory will be created if it does not exist. The file will be created if it does not exist. An exception is
   * thrown if the file object exists but is a directory. An exception is thrown if the file exists but cannot be written to. An
   * exception is thrown if the parent directory cannot be created.
   * 
   * @param file
   *          the file to open for output, must not be <code>null</code>
   * @return a new {@link FileOutputStream} for the specified file
   * @throws IOException
   *           if the file object is a directory
   * @throws IOException
   *           if the file cannot be written to
   * @throws IOException
   *           if a parent directory needs creating but that fails
   */
  public static FileOutputStream openOutputStream(File file) throws IOException {
    if (file.exists()) {
      if (file.isDirectory()) {
        throw new IOException("File '" + file + "' exists but is a directory");
      }
      if (file.canWrite() == false) {
        throw new IOException("File '" + file + "' cannot be written to");
      }
    } else {
      File parent = file.getParentFile();
      if (parent != null && parent.exists() == false) {
        if (parent.mkdirs() == false) {
          throw new IOException("File '" + file + "' could not be created");
        }
      }
    }
    return new FileOutputStream(file);
  }

  //-----------------------------------------------------------------------
  /**
   * Opens a {@link FileInputStream} for the specified file, providing better error messages than simply calling
   * <code>new FileInputStream(file)</code>.
   * <p>
   * At the end of the method either the stream will be successfully opened, or an exception will have been thrown.
   * <p>
   * An exception is thrown if the file does not exist. An exception is thrown if the file object exists but is a directory. An
   * exception is thrown if the file exists but cannot be read.
   * 
   * @param file
   *          the file to open for input, must not be <code>null</code>
   * @return a new {@link FileInputStream} for the specified file
   * @throws FileNotFoundException
   *           if the file does not exist
   * @throws IOException
   *           if the file object is a directory
   * @throws IOException
   *           if the file cannot be read
   * @since Commons IO 1.3
   */
  public static FileInputStream openInputStream(File file) throws IOException {
    if (file.exists()) {
      if (file.isDirectory()) {
        throw new IOException("Il file '" + file + "' esiste ma e' una directory");
      }
      if (file.canRead() == false) {
        throw new IOException("Non riesco a leggere il file '" + file + "'");
      }
    } else {
      throw new FileNotFoundException("Il file '" + file + "' non esiste");
    }
    return new FileInputStream(file);
  }

  /**
   * Genera un url unico nel formato: [Path, NomeFile, Estensione] ma non cerca di acquisire il lock di un FileOutputStream
   * 
   * @throws IOException
   */
  /*
   * public static String[] generaURL(String path, String nomeFile, String estensione) throws IllegalArgumentException, IOException { Object[]
   * arrayPathNomeExtFile = acquisisciFileOutputStream(path, nomeFile, estensione); FileOutputStream fos = (FileOutputStream)
   * arrayPathNomeExtFile[FOS]; try { fos.close(); } catch (IOException e) { e.printStackTrace(); } String[] arrayPathNomeExt = new
   * String[]{ (String) arrayPathNomeExtFile[PATH], (String) arrayPathNomeExtFile[NOME], (String) arrayPathNomeExtFile[ESTENSIONE]
   * }; return arrayPathNomeExt; }
   */

  public static String leggiInputStream(InputStream is, int dimensioniMassime) throws IOException, IllegalArgumentException {
    byte[] tmp = null;
    byte[] buffer = new byte[0];
    int byteDisponibili = 0;
    int byteLettiTotali = 0;
    int byteLetti = 0;

    while ( (byteDisponibili = is.available()) != 0) {
      tmp = new byte[byteDisponibili];
      byteLetti = is.read(tmp, byteLettiTotali, byteDisponibili);
      //if( byteLetti == -1) break;
      byteLettiTotali += byteLetti;
      if (byteLettiTotali > dimensioniMassime)
        throw new IllegalArgumentException("La lunghezza dello stream da leggere è maggiore della dimensione massima definita di byte: ["
            + dimensioniMassime + "]");

      int dimensionePreResize = buffer.length;
      buffer = Arrays.copyOf(buffer, dimensionePreResize + byteLetti);
      System.arraycopy(tmp, 0, buffer, dimensionePreResize, byteLetti);
    }

    return new String(buffer);
  }

  public static FileLock lock(String path) throws IOException, IllegalArgumentException {
    return lock(new File(path));
  }

  public static synchronized FileLock lock(File file) throws IOException, IllegalArgumentException {
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    FileChannel fileChannel = fileOutputStream.getChannel();

    FileLock fileLock = fileChannel.tryLock();
    if (fileLock == null) {
      throw new IllegalArgumentException("Lock del file " + file.toURI().toURL() + " gia' acquisito!");
    }

    return fileLock;
  }

  public static void unlock(FileLock fileLock) throws IOException {
    if (fileLock != null) {
      fileLock.release();
      Channel channel = fileLock.channel();
      channel.close();
    }
  }

  /**
   * Per quei casi dove si vuole sbloccare il file di lock e rimuovere il file che crea per loccare
   * 
   * @param fileLock
   *          Lock da sbloccare
   * @param file
   *          File da cancellare dopo aver sbloccato il lock
   * @throws IOException
   *           In caso sia impossibile rilasciare il lock o cancellare il file
   */
  public static synchronized void unlockAndDestroyFile(FileLock fileLock, File file) throws IOException {
    unlock(fileLock);
    file.delete();
  }

  /**
   * Copia files
   * 
   * @param src
   *          String Path del file sorgente
   * @param dest
   *          String path del file di destinazione
   * @throws IOException
   */
  public static void copyFile(String src, String dest) throws IOException {
    copyFile(src, dest, null);
  }

  /**
   * Copia files
   * 
   * @param src
   *          String Path del file sorgente
   * @param dest
   *          String path del file di destinazione
   * @param header
   *          String eventuale header nella destinazione (può essere null)
   * @throws IOException
   */
  public static void copyFile(String src, String dest, String header) throws IOException {
    // Use unbuffered streams, because we're going to use a large buffer
    // for this sequential io.
    FileInputStream input = new FileInputStream(src);
    FileOutputStream output = new FileOutputStream(dest);

    if (header != null) {
      int headerLength = header.length();
      byte[] headerBytes = header.getBytes();
      output.write(headerBytes, 0, headerLength);
    }

    int bytesRead;
    byte[] buffer = new byte[32 * 1024];
    while ( (bytesRead = input.read(buffer, 0, buffer.length)) > 0)
      output.write(buffer, 0, bytesRead);

    input.close();
    output.close();
  }

  /**
   * Cancella un file di cui è passato il nome (possibilmente con path)
   * 
   * @param name
   *          nome (path) del file
   */
  public static final void deleteFile(String name) {
    new File(name).delete();
  }

  //-----------------------------------------------------------------------
  /**
   * Deletes a file. If file is a directory, delete it and all sub-directories.
   * <p>
   * The difference between File.delete() and this method are:
   * <ul>
   * <li>A directory to be deleted does not have to be empty.</li>
   * <li>You get exceptions when a file or directory cannot be deleted. (java.io.File methods returns a boolean)</li>
   * </ul>
   * 
   * @param file
   *          file or directory to delete, must not be <code>null</code>
   * @throws NullPointerException
   *           if the directory is <code>null</code>
   * @throws FileNotFoundException
   *           if the file was not found
   * @throws IOException
   *           in case deletion is unsuccessful
   */
  public static void forceDelete(File file) throws IOException {
    if (file.isDirectory()) {
      deleteDirectory(file);
    } else {
      boolean filePresent = file.exists();
      if ( !file.delete()) {
        if ( !filePresent) {
          throw new FileNotFoundException("File does not exist: " + file);
        }
        String message = "Unable to delete file: " + file;
        throw new IOException(message);
      }
    }
  }

  /**
   * Deletes a directory recursively.
   * 
   * @param directory
   *          directory to delete
   * @throws IOException
   *           in case deletion is unsuccessful
   */
  public static void deleteDirectory(File directory) throws IOException {
    if ( !directory.exists()) {
      return;
    }

    cleanDirectory(directory);
    if ( !directory.delete()) {
      String message = "Unable to delete directory " + directory + ".";
      throw new IOException(message);
    }
  }

  /**
   * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
   * <p>
   * The difference between File.delete() and this method are:
   * <ul>
   * <li>A directory to be deleted does not have to be empty.</li>
   * <li>No exceptions are thrown when a file or directory cannot be deleted.</li>
   * </ul>
   * 
   * @param file
   *          file or directory to delete, can be <code>null</code>
   * @return <code>true</code> if the file or directory was deleted, otherwise <code>false</code>
   * 
   * @since Commons IO 1.4
   */
  public static boolean deleteQuietly(File file) {
    if (file == null) {
      return false;
    }
    try {
      if (file.isDirectory()) {
        cleanDirectory(file);
      }
    } catch (Exception e) {
    }

    try {
      return file.delete();
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Pulisce una directory senza cancellarla, cioè cancella tutti i suoi files. Le sottodirectory sono interamente cancellate.
   * 
   * @param directory
   *          directory da pulire
   * @throws IOException
   *           in caso di errori in pulizia
   */
  public static void cleanDirectory(File directory) throws IOException {
    if ( !directory.exists()) {
      String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if ( !directory.isDirectory()) {
      String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    File[] files = directory.listFiles();
    if (files == null) { // null if security restricted
      throw new IOException("Failed to list contents of " + directory);
    }

    IOException exception = null;
    for (int i = 0; i < files.length; i++) {
      File file = files[i];
      try {
        forceDelete(file);
      } catch (IOException ioe) {
        exception = ioe;
      }
    }

    if (null != exception) {
      throw exception;
    }
  }

  /**
   * Reads the contents of a file into a String. The file is always closed.
   * 
   * @param file
   *          the file to read, must not be <code>null</code>
   * @param encoding
   *          the encoding to use, <code>null</code> means platform default
   * @return the file contents, never <code>null</code>
   * @throws IOException
   *           in case of an I/O error
   * @throws java.io.UnsupportedEncodingException
   *           if the encoding is not supported by the VM
   */
  public static String readFileToString(File file, String encoding) throws IOException {
    InputStream in = null;
    try {
      in = openInputStream(file);
      return toString(in, encoding);
    } finally {
      closeQuietly(in);
    }
  }

  public static String streamToString(InputStream in, String encoding) throws IOException {
    try {
      return toString(in, encoding);
    } finally {
      closeQuietly(in);
    }
  }

  public static String streamToString(InputStream in) throws IOException {
    return streamToString(in, null);
  }

  /**
   * Trasforma una stringa in un input stream
   * @param s
   * @return
   * @throws UnsupportedEncodingException
   */
  public static InputStream stringToStream(String s) throws UnsupportedEncodingException  {
    return new ByteArrayInputStream(s.getBytes("UTF-8"));
  }
  
  /**
   * Reads the contents of a file into a String using the default encoding for the VM. The file is always closed.
   * 
   * @param file
   *          the file to read, must not be <code>null</code>
   * @return the file contents, never <code>null</code>
   * @throws IOException
   *           in case of an I/O error
   * @since Commons IO 1.3.1
   */
  public static String readFileToString(File file) throws IOException {
    return readFileToString(file, null);
  }

  /**
   * Reads the contents of a file line by line to a List of Strings. The file is always closed.
   * 
   * @param file
   *          the file to read, must not be <code>null</code>
   * @param encoding
   *          the encoding to use, <code>null</code> means platform default
   * @return the list of Strings representing each line in the file, never <code>null</code>
   * @throws IOException
   *           in case of an I/O error
   * @throws java.io.UnsupportedEncodingException
   *           if the encoding is not supported by the VM
   * @since Commons IO 1.1
   */
  public static List<String> readLines(File file, String encoding) throws IOException {
    InputStream in = null;
    try {
      in = openInputStream(file);
      return readLines(in, encoding);
    } finally {
      closeQuietly(in);
    }
  }

  /**
   * Questo metodo controlla se il file passato è possibilmente nella codifica UTF-8. <br>
   * NOTA BENE: è impossibile determinare con certezza la codifica di un file dal suo contenuto; questa routine semplicemente
   * controlla i primi 3 bytes del file per vedere se contengono il cosiddetto BOM: se c'è è con buona probabilità un file con
   * codifica UTF-8.
   * 
   * @param file
   *          file da controllare
   * @return true se il file è con buona probabilità in codifica UTF-8
   * @throws IOException
   */
  public static boolean isUTF8(File file) throws IOException {
    InputStream in = null;
    try {
      in = openInputStream(file);
      byte[] bom = new byte[3];
      int count = in.read(bom, 0, 3);
      if (count != 3)
        return false;
      return (bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF);
    } finally {
      closeQuietly(in);
    }
  }

  /**
   * Reads the contents of a file line by line to a List of Strings using the default encoding for the VM. The file is always
   * closed.
   * 
   * @param file
   *          the file to read, must not be <code>null</code>
   * @return the list of Strings representing each line in the file, never <code>null</code>
   * @throws IOException
   *           in case of an I/O error
   * @since Commons IO 1.3
   */
  public static List<String> readLines(File file) throws IOException {
    return readLines(file, null);
  }

  /**
   * Get the contents of an <code>InputStream</code> as a list of Strings, one entry per line, using the specified character
   * encoding.
   * <p>
   * Character encoding names can be found at <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
   * 
   * @param input
   *          the <code>InputStream</code> to read from, not null
   * @param encoding
   *          the encoding to use, null means platform default
   * @return the list of Strings, never null
   * @throws NullPointerException
   *           if the input is null
   * @throws IOException
   *           if an I/O error occurs
   * @since Commons IO 1.1
   */
  public static List<String> readLines(InputStream input, String encoding) throws IOException {
    if (encoding == null) {
      return readLines(input);
    } else {
      InputStreamReader reader = new InputStreamReader(input, encoding);
      return readLines(reader);
    }
  }

  /**
   * Get the contents of an <code>InputStream</code> as a list of Strings, one entry per line, using the default character encoding
   * of the platform.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
   * 
   * @param input
   *          the <code>InputStream</code> to read from, not null
   * @return the list of Strings, never null
   * @throws NullPointerException
   *           if the input is null
   * @throws IOException
   *           if an I/O error occurs
   * @since Commons IO 1.1
   */
  public static List<String> readLines(InputStream input) throws IOException {
    InputStreamReader reader = new InputStreamReader(input);
    return readLines(reader);
  }

  /**
   * Get the contents of a <code>Reader</code> as a list of Strings, one entry per line.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedReader</code>.
   * 
   * @param input
   *          the <code>Reader</code> to read from, not null
   * @return the list of Strings, never null
   * @throws NullPointerException
   *           if the input is null
   * @throws IOException
   *           if an I/O error occurs
   * @since Commons IO 1.1
   */
  public static List<String> readLines(Reader input) throws IOException {
    BufferedReader reader = new BufferedReader(input);
    List<String> list = new ArrayList<String>();
    String line = reader.readLine();
    while (line != null) {
      list.add(line);
      line = reader.readLine();
    }
    return list;
  }

  /**
   * Unconditionally close an <code>Reader</code>.
   * <p>
   * Equivalent to {@link Reader#close()}, except any exceptions will be ignored. This is typically used in finally blocks.
   * 
   * @param input
   *          the Reader to close, may be null or already closed
   */
  public static void closeQuietly(Reader input) {
    try {
      if (input != null) {
        input.close();
      }
    } catch (IOException ioe) {
      // ignore
    }
  }

  /**
   * Unconditionally close a <code>Writer</code>.
   * <p>
   * Equivalent to {@link Writer#close()}, except any exceptions will be ignored. This is typically used in finally blocks.
   * 
   * @param output
   *          the Writer to close, may be null or already closed
   */
  public static void closeQuietly(Writer output) {
    try {
      if (output != null) {
        output.close();
      }
    } catch (IOException ioe) {
      // ignore
    }
  }

  /**
   * Unconditionally close an <code>InputStream</code>.
   * <p>
   * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored. This is typically used in finally blocks.
   * 
   * @param input
   *          the InputStream to close, may be null or already closed
   */
  public static void closeQuietly(InputStream input) {
    try {
      if (input != null) {
        input.close();
      }
    } catch (IOException ioe) {
      // ignore
    }
  }

  /**
   * Unconditionally close an <code>OutputStream</code>.
   * <p>
   * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored. This is typically used in finally blocks.
   * 
   * @param output
   *          the OutputStream to close, may be null or already closed
   */
  public static void closeQuietly(OutputStream output) {
    try {
      if (output != null) {
        output.close();
      }
    } catch (IOException ioe) {
      // ignore
    }
  }

  /**
   * Get the contents of an <code>InputStream</code> as a String using the default character encoding of the platform.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
   * 
   * @param input
   *          the <code>InputStream</code> to read from
   * @return the requested String
   * @throws NullPointerException
   *           if the input is null
   * @throws IOException
   *           if an I/O error occurs
   */
  public static String toString(InputStream input) throws IOException {
    StringWriter sw = new StringWriter();
    copy(input, sw);
    return sw.toString();
  }

  /**
   * Get the contents of an <code>InputStream</code> as a String using the specified character encoding.
   * <p>
   * Character encoding names can be found at <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
   * 
   * @param input
   *          the <code>InputStream</code> to read from
   * @param encoding
   *          the encoding to use, null means platform default
   * @return the requested String
   * @throws NullPointerException
   *           if the input is null
   * @throws IOException
   *           if an I/O error occurs
   */
  public static String toString(InputStream input, String encoding) throws IOException {
    StringWriter sw = new StringWriter();
    copy(input, sw, encoding);
    return sw.toString();
  }

  /**
   * Get the contents of a <code>Reader</code> as a String.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedReader</code>.
   * 
   * @param input
   *          the <code>Reader</code> to read from
   * @return the requested String
   * @throws NullPointerException
   *           if the input is null
   * @throws IOException
   *           if an I/O error occurs
   */
  public static String toString(Reader input) throws IOException {
    StringWriter sw = new StringWriter();
    copy(input, sw);
    return sw.toString();
  }

  /**
   * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
   * <p>
   * Large streams (over 2GB) will return a bytes copied value of <code>-1</code> after the copy has completed since the correct
   * number of bytes cannot be returned as an int. For large streams use the <code>copyLarge(InputStream, OutputStream)</code>
   * method.
   * 
   * @param input
   *          the <code>InputStream</code> to read from
   * @param output
   *          the <code>OutputStream</code> to write to
   * @return the number of bytes copied
   * @throws NullPointerException
   *           if the input or output is null
   * @throws IOException
   *           if an I/O error occurs
   * @throws ArithmeticException
   *           if the byte count is too large
   * @since Commons IO 1.1
   */
  public static int copy(InputStream input, OutputStream output) throws IOException {
    long count = copyLarge(input, output);
    if (count > Integer.MAX_VALUE) {
      return -1;
    }
    return (int) count;
  }

  /**
   * Copy bytes from a large (over 2GB) <code>InputStream</code> to an <code>OutputStream</code>.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
   * 
   * @param input
   *          the <code>InputStream</code> to read from
   * @param output
   *          the <code>OutputStream</code> to write to
   * @return the number of bytes copied
   * @throws NullPointerException
   *           if the input or output is null
   * @throws IOException
   *           if an I/O error occurs
   * @since Commons IO 1.3
   */
  public static long copyLarge(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    long count = 0;
    int n = 0;
    while ( -1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  /**
   * Copy bytes from an <code>InputStream</code> to chars on a <code>Writer</code> using the default character encoding of the
   * platform.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
   * <p>
   * This method uses {@link InputStreamReader}.
   * 
   * @param input
   *          the <code>InputStream</code> to read from
   * @param output
   *          the <code>Writer</code> to write to
   * @throws NullPointerException
   *           if the input or output is null
   * @throws IOException
   *           if an I/O error occurs
   * @since Commons IO 1.1
   */
  public static void copy(InputStream input, Writer output) throws IOException {
    InputStreamReader in = new InputStreamReader(input);
    copy(in, output);
  }

  /**
   * Copy bytes from an <code>InputStream</code> to chars on a <code>Writer</code> using the specified character encoding.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
   * <p>
   * Character encoding names can be found at <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
   * <p>
   * This method uses {@link InputStreamReader}.
   * 
   * @param input
   *          the <code>InputStream</code> to read from
   * @param output
   *          the <code>Writer</code> to write to
   * @param encoding
   *          the encoding to use, null means platform default
   * @throws NullPointerException
   *           if the input or output is null
   * @throws IOException
   *           if an I/O error occurs
   * @since Commons IO 1.1
   */
  public static void copy(InputStream input, Writer output, String encoding) throws IOException {
    if (encoding == null) {
      copy(input, output);
    } else {
      InputStreamReader in = new InputStreamReader(input, encoding);
      copy(in, output);
    }
  }

  /**
   * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedReader</code>.
   * <p>
   * Large streams (over 2GB) will return a chars copied value of <code>-1</code> after the copy has completed since the correct
   * number of chars cannot be returned as an int. For large streams use the <code>copyLarge(Reader, Writer)</code> method.
   * 
   * @param input
   *          the <code>Reader</code> to read from
   * @param output
   *          the <code>Writer</code> to write to
   * @return the number of characters copied
   * @throws NullPointerException
   *           if the input or output is null
   * @throws IOException
   *           if an I/O error occurs
   * @throws ArithmeticException
   *           if the character count is too large
   * @since Commons IO 1.1
   */
  public static int copy(Reader input, Writer output) throws IOException {
    long count = copyLarge(input, output);
    if (count > Integer.MAX_VALUE) {
      return -1;
    }
    return (int) count;
  }

  /**
   * Copy chars from a large (over 2GB) <code>Reader</code> to a <code>Writer</code>.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedReader</code>.
   * 
   * @param input
   *          the <code>Reader</code> to read from
   * @param output
   *          the <code>Writer</code> to write to
   * @return the number of characters copied
   * @throws NullPointerException
   *           if the input or output is null
   * @throws IOException
   *           if an I/O error occurs
   * @since Commons IO 1.3
   */
  public static long copyLarge(Reader input, Writer output) throws IOException {
    char[] buffer = new char[DEFAULT_BUFFER_SIZE];
    long count = 0;
    int n = 0;
    while ( -1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  /**
   * Copy chars from a <code>Reader</code> to bytes on an <code>OutputStream</code> using the default character encoding of the
   * platform, and calling flush.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedReader</code>.
   * <p>
   * Due to the implementation of OutputStreamWriter, this method performs a flush.
   * <p>
   * This method uses {@link OutputStreamWriter}.
   * 
   * @param input
   *          the <code>Reader</code> to read from
   * @param output
   *          the <code>OutputStream</code> to write to
   * @throws NullPointerException
   *           if the input or output is null
   * @throws IOException
   *           if an I/O error occurs
   * @since Commons IO 1.1
   */
  public static void copy(Reader input, OutputStream output) throws IOException {
    OutputStreamWriter out = new OutputStreamWriter(output);
    copy(input, out);
    out.flush();
  }

  /**
   * Copy chars from a <code>Reader</code> to bytes on an <code>OutputStream</code> using the specified character encoding, and
   * calling flush.
   * <p>
   * This method buffers the input internally, so there is no need to use a <code>BufferedReader</code>.
   * <p>
   * Character encoding names can be found at <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
   * <p>
   * Due to the implementation of OutputStreamWriter, this method performs a flush.
   * <p>
   * This method uses {@link OutputStreamWriter}.
   * 
   * @param input
   *          the <code>Reader</code> to read from
   * @param output
   *          the <code>OutputStream</code> to write to
   * @param encoding
   *          the encoding to use, null means platform default
   * @throws NullPointerException
   *           if the input or output is null
   * @throws IOException
   *           if an I/O error occurs
   * @since Commons IO 1.1
   */
  public static void copy(Reader input, OutputStream output, String encoding) throws IOException {
    if (encoding == null) {
      copy(input, output);
    } else {
      OutputStreamWriter out = new OutputStreamWriter(output, encoding);
      copy(input, out);
      out.flush();
    }
  }

  /**
   * Verifica l'esistenza di una directory (solo l'ultimo livello) Es.
   */
  public static boolean existsDir(String szNomeDir) {
    File directory = new File(szNomeDir);
    if ( !directory.exists() || !directory.isDirectory()) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Cerca di creare una directory (solo questa cartella) Es.
   */
  public static void createDir(String szNomeDir) throws IllegalArgumentException {
    if ( !existsDir(szNomeDir)) {
      File directory = new File(szNomeDir);
      if ( !directory.mkdir()) {
        throw new IllegalArgumentException("Impossibile creare la directory '" + szNomeDir + "'!");
      }
    }
  }

  /**
   * Calcola il checksum del file passato
   * @param f file di cui calcolare il checksum
   * @return checksum calcolato
   * @throws IOException in caso di errori di lettura del file
   * @see CRC32
   */
  public static long checksumCRC32(File f) throws IOException {
    return checksumCRC32(new FileInputStream(f));
  }
  
  /**
   * Calcola il checksum del InputStream passato
   * @param is InputStream di cui calcolare il checksum
   * @return checksum calcolato
   * @throws IOException in caso di errori di lettura dello stream
   * @see CRC32
   */
  public static long checksumCRC32(InputStream is) throws IOException {
    InputStream in = null;
    try {
      CRC32 crc = new CRC32();
      in = new CheckedInputStream(is, crc);
      IOUtils.copy(in, new NullOutputStream());
      return crc.getValue();
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Dato un path, restituisce l'ultima parte.
   * Se il path finisce con un separatore di directory ('/' o '\\')
   * ritorna la stringa vuota. Esempi:<br/>
   * <tt>getLastPart("C:/temp/docs/mydoc.txt")</tt> --> <b>"mydoc.txt"</b> 
   * <br/> <tt>getLastPart("C:/temp/docs/")</tt> --> <b>""</b>
   *  
   * @param filePath path da analizzare
   * @return ultima parte del path
   */
  public static String getLastPart(String filePath) {
    if (filePath==null || filePath.trim().length() == 0) {
      return "";
    }
    filePath = filePath.trim();
    int i1 = filePath.lastIndexOf('/');
    int i2 = filePath.lastIndexOf('\\');
    int last = i1 > i2 ? i1 : i2;
    if (last >= filePath.length() - 1) {
      return "";
    }
    else {
      return filePath.substring(last + 1, filePath.length());
    }
  }
}
