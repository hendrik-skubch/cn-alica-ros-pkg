//package de.uni_kassel.cn.planDesigner.codegeneration;
//import java.io.BufferedInputStream;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.Properties;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.eclipse.jdt.core.ToolFactory;
//import org.eclipse.jdt.core.formatter.CodeFormatter;
//import org.eclipse.jface.text.BadLocationException;
//import org.eclipse.jface.text.Document;
//import org.eclipse.jface.text.IDocument;
//import org.eclipse.text.edits.MalformedTreeException;
//import org.eclipse.text.edits.TextEdit;
//import org.openarchitectureware.workflow.util.ResourceLoaderFactory;
//import org.openarchitectureware.xpand2.output.FileHandle;
//
///**
// * Formats generated Java code using the Eclipse code formatter.
// * 
// * @author Sven Efftinge (http://www.efftinge.de) (initial)
// * @author Bernd Kolb
// * @author Peter Friese
// * @author Markus V�lter
// * @author Michael Leopoldseder
// * @author Karsten Thoms
// * @since 4.0
// */
//public class CNBeautifier implements PostProcessor {
//
//	/** Logger instance. */
//	private final Log log = LogFactory.getLog(getClass());
//
//	/** Singleton code formatter instance. */
//	private CodeFormatter codeFormatter;
//
//	/**
//	 * Path to the config file for the formatter. See
//	 * http://www.peterfriese.de/index.php/2007/05/28/formatting-your-code-using-the-eclipse-code-formatter/
//	 * for more infor on creating the config file.
//	 */
//	private String configFile;
//
//	private Properties options;
//
//	/**
//	 * Formats the file using Eclipse code formatter. The file must have the
//	 * extension '.java'.
//	 */
//	public void beforeWriteAndClose(final FileHandle info) {
//		if (info.getTargetFile() != null && info.getTargetFile().getAbsolutePath().endsWith(".cs")) {
//
//			IDocument doc = new Document(info.getBuffer().toString());
//			TextEdit edit = getCodeFormatter().format(CodeFormatter.K_COMPILATION_UNIT, doc.get(), 0,
//					doc.get().length(), 0, null);
//
//			// check if text formatted successfully
//			if (edit != null) {
//				try {
//					edit.apply(doc);
//					info.setBuffer(new StringBuffer(doc.get()));
//				} catch (MalformedTreeException e) {
//					log.warn("Error during code formatting. Illegal code edit tree (" + e.getMessage() + ").");
//				} catch (BadLocationException e) {
//					log.warn("Error during code formatting. Bad location (" + e.getMessage() + ").");
//				}
//			} else {
//				log.warn("File " + info.getTargetFile()
//						+ " could not be formatted. Make sure your template produces legal Java code!");
//			}
//		}
//	}
//
//	/**
//	 * Returns an instance of the Eclipse code formatter. If the user supplied
//	 * the path to a config file, this file will be used to configure the code
//	 * formatter. Otherwise we use the default options supplied with oAW.
//	 * 
//	 * @return a preconfigured instance of the Eclipse code formatter.
//	 */
//	private CodeFormatter getCodeFormatter() {
//		if (codeFormatter == null) {
//
//			if ( configFile == null ) {
//				options = new Properties();
//				options.put("org.eclipse.jdt.core.compiler.compliance","1.5");
//				options.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform","1.5");
//				options.put("org.eclipse.jdt.core.compiler.source","1.5");				
//				log.debug("no config file specified; using the default config file supplied with oAW: org.eclipse.jdt.core.formatterprefs");
//			} else {
//				options = readConfig(configFile);
//			}
//			
//			// instantiate the formatter
//			codeFormatter = ToolFactory.createCodeFormatter(options);
//		}
//		return codeFormatter;
//	}
//
//
//	/**
//	 * Return a Java Properties file representing the options that are in the
//	 * specified config file.
//	 */
//	private Properties readConfig(String filename) {
//		BufferedInputStream stream = null;
//        BufferedReader reader = null;
//        
//		try {
//           InputStream is = openStream(filename);
//           final Properties formatterOptions = new Properties();
//            if ( filename.endsWith(".xml")) {
//               Pattern pattern = Pattern.compile("<setting id=\"([^\"]*)\" value=\"([^\"]*)\"\\/>");
//               reader = new BufferedReader(new InputStreamReader(is));
//               for (String line = reader.readLine(); line != null; line = reader.readLine()) {
//                  Matcher matcher = pattern.matcher(line);
//                  if ( matcher.matches() ) {
//                     formatterOptions.put(matcher.group(1), matcher.group(2));
//                  }
//               }
//            }
//            else {
//               stream = new BufferedInputStream(is);
//               formatterOptions.load(stream);
//            }			
//            // add some settings for the compiler options
//            // which are not included in the Eclipse code style settings
//            // to make the code formatter working
//            // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=222736
//            if( formatterOptions.get("org.eclipse.jdt.core.compiler.compliance") == null ) {
//         	   formatterOptions.put("org.eclipse.jdt.core.compiler.compliance", "1.5");
//            }
//            if( formatterOptions.get("org.eclipse.jdt.core.compiler.codegen.targetPlatform") == null ) {
//         	   formatterOptions.put("org.eclipse.jdt.core.compiler.codegen.targetPlatform", "1.5");
//            }
//            if( formatterOptions.get("org.eclipse.jdt.core.compiler.source") == null ) {
//         	   formatterOptions.put("org.eclipse.jdt.core.compiler.source", "1.5");
//            }
//            return formatterOptions;
//		} catch (IOException e) {
//			log.warn("Problem reading code formatter config file (" + e.getMessage() + ").");
//		} finally {
//			if (stream != null) {
//				try {
//					stream.close();
//				} catch (IOException e) {
//					/* ignore */
//				}
//			}
//            if ( reader != null ) {
//               try {
//                  reader.close();
//               } catch (IOException e) {
//                    /* ignore */
//               }
//            }
//		}
//		return null;
//	}
//
//	 /**
//	  * Searches for the given filename as a ressource and returns a stream on it. Throws an IOException, if the file
//	  * cannot be found.
//	  * 
//	  * @param filename
//	  *				   The name of the file to be searched in the ressources.
//	  * @return InputStream for subsequent reading
//	  * @throws IOException
//	  */
//	 protected InputStream openStream(String filename) throws IOException {
//		 InputStream is = ResourceLoaderFactory.createResourceLoader().getResourceAsStream(filename);
//		 if (is == null) {
//			 throw new IOException("Config file [" + filename + "] does not exist.");
//		 }
//		 return is;
//	 }
//	
//	/**
//	 * @return the configuration file for the formatter
//	 */
//	public String getConfigFile() {
//		return configFile;
//	}
//
//	/**
//	 * @param configFile
//	 *            configuration file for the formatter
//	 */
//	public void setConfigFile(String configFile) {
//		this.configFile = configFile;
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	public void afterClose(final FileHandle impl) {
//		// do nothing here
//	}
//
//}

