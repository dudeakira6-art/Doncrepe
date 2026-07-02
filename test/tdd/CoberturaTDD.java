package tdd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.RuntimeData;
import org.jacoco.core.runtime.SystemPropertiesRuntime;

public class CoberturaTDD {
    private static final Path ROOT = Paths.get("").toAbsolutePath();
    private static final Path BUILD_CLASSES = ROOT.resolve("build/classes");
    private static final Path TEST_CLASSES = ROOT.resolve("build/test/classes");
    private static final Path INSTRUMENTED_CLASSES = ROOT.resolve("build/jacoco/classes");
    private static final Path REPORT_DIR = ROOT.resolve("build/reports/jacoco");
    private static final Path XML_REPORT = REPORT_DIR.resolve("jacoco.xml");

    public static void main(String[] args) throws Exception {
        limpiarDirectorio(INSTRUMENTED_CLASSES);
        Files.createDirectories(INSTRUMENTED_CLASSES);
        Files.createDirectories(REPORT_DIR);

        SystemPropertiesRuntime runtime = new SystemPropertiesRuntime();
        RuntimeData runtimeData = new RuntimeData();
        runtime.startup(runtimeData);
        try {
            instrumentarClases(runtime);
            ejecutarPruebas();
        } finally {
            runtime.shutdown();
        }

        ExecutionDataStore executionData = new ExecutionDataStore();
        SessionInfoStore sessionInfo = new SessionInfoStore();
        runtimeData.collect(executionData, sessionInfo, false);

        CoverageBuilder coverageBuilder = new CoverageBuilder();
        Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        analyzer.analyzeAll(BUILD_CLASSES.toFile());

        escribirXml(sessionInfo, coverageBuilder.getBundle("DonCrepePOS"));
        System.out.println("Cobertura generada en: " + XML_REPORT.toAbsolutePath());
    }

    private static void instrumentarClases(SystemPropertiesRuntime runtime) throws IOException {
        Instrumenter instrumenter = new Instrumenter(runtime);
        Files.walk(BUILD_CLASSES)
                .filter(Files::isRegularFile)
                .forEach(origen -> {
                    Path destino = INSTRUMENTED_CLASSES.resolve(BUILD_CLASSES.relativize(origen).toString());
                    try {
                        Files.createDirectories(destino.getParent());
                        if (origen.toString().endsWith(".class")) {
                            try (InputStream in = new BufferedInputStream(new FileInputStream(origen.toFile()));
                                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(destino.toFile()))) {
                                instrumenter.instrument(in, out, origen.getFileName().toString());
                            }
                        } else {
                            Files.copy(origen, destino, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException("No se pudo instrumentar " + origen, ex);
                    }
                });
    }

    private static void ejecutarPruebas() throws Exception {
        List<URL> urls = new ArrayList<URL>();
        urls.add(INSTRUMENTED_CLASSES.toUri().toURL());
        urls.add(TEST_CLASSES.toUri().toURL());
        for (File jar : obtenerJarsLib()) {
            urls.add(jar.toURI().toURL());
        }
        try (URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader())) {
            Thread.currentThread().setContextClassLoader(loader);
            Class<?> pruebas = Class.forName("tdd.PruebasTDD", true, loader);
            pruebas.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
        }
    }

    private static List<File> obtenerJarsLib() {
        File lib = ROOT.resolve("lib").toFile();
        File[] jars = lib.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jars == null) {
            return Arrays.asList();
        }
        return Arrays.asList(jars);
    }

    private static void escribirXml(SessionInfoStore sessions, IBundleCoverage bundle) throws Exception {
        XMLOutputFactory factory = XMLOutputFactory.newFactory();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(XML_REPORT.toFile()), StandardCharsets.UTF_8)) {
            XMLStreamWriter xml = factory.createXMLStreamWriter(writer);
            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("report");
            xml.writeAttribute("name", "DonCrepePOS");

            for (org.jacoco.core.data.SessionInfo info : sessions.getInfos()) {
                xml.writeEmptyElement("sessioninfo");
                xml.writeAttribute("id", info.getId());
                xml.writeAttribute("start", String.valueOf(info.getStartTimeStamp()));
                xml.writeAttribute("dump", String.valueOf(info.getDumpTimeStamp()));
            }

            for (IPackageCoverage pkg : bundle.getPackages()) {
                xml.writeStartElement("package");
                xml.writeAttribute("name", pkg.getName());

                for (IClassCoverage clazz : pkg.getClasses()) {
                    xml.writeStartElement("class");
                    xml.writeAttribute("name", clazz.getName());
                    xml.writeAttribute("sourcefilename", clazz.getSourceFileName());
                    escribirCounters(xml, clazz);
                    for (IMethodCoverage metodo : clazz.getMethods()) {
                        xml.writeStartElement("method");
                        xml.writeAttribute("name", metodo.getName());
                        xml.writeAttribute("desc", metodo.getDesc());
                        xml.writeAttribute("line", String.valueOf(metodo.getFirstLine()));
                        escribirCounters(xml, metodo);
                        xml.writeEndElement();
                    }
                    xml.writeEndElement();
                }

                for (ISourceFileCoverage source : pkg.getSourceFiles()) {
                    xml.writeStartElement("sourcefile");
                    xml.writeAttribute("name", source.getName());
                    for (int linea = source.getFirstLine(); linea <= source.getLastLine(); linea++) {
                        ILine info = source.getLine(linea);
                        if (info.getInstructionCounter().getTotalCount() == 0 && info.getBranchCounter().getTotalCount() == 0) {
                            continue;
                        }
                        xml.writeEmptyElement("line");
                        xml.writeAttribute("nr", String.valueOf(linea));
                        xml.writeAttribute("mi", String.valueOf(info.getInstructionCounter().getMissedCount()));
                        xml.writeAttribute("ci", String.valueOf(info.getInstructionCounter().getCoveredCount()));
                        xml.writeAttribute("mb", String.valueOf(info.getBranchCounter().getMissedCount()));
                        xml.writeAttribute("cb", String.valueOf(info.getBranchCounter().getCoveredCount()));
                    }
                    escribirCounters(xml, source);
                    xml.writeEndElement();
                }

                escribirCounters(xml, pkg);
                xml.writeEndElement();
            }

            escribirCounters(xml, bundle);
            xml.writeEndElement();
            xml.writeEndDocument();
            xml.flush();
            xml.close();
        }
    }

    private static void escribirCounters(XMLStreamWriter xml, org.jacoco.core.analysis.ICoverageNode node) throws Exception {
        escribirCounter(xml, "INSTRUCTION", node.getInstructionCounter());
        escribirCounter(xml, "BRANCH", node.getBranchCounter());
        escribirCounter(xml, "LINE", node.getLineCounter());
        escribirCounter(xml, "COMPLEXITY", node.getComplexityCounter());
        escribirCounter(xml, "METHOD", node.getMethodCounter());
        escribirCounter(xml, "CLASS", node.getClassCounter());
    }

    private static void escribirCounter(XMLStreamWriter xml, String tipo, ICounter counter) throws Exception {
        xml.writeEmptyElement("counter");
        xml.writeAttribute("type", tipo);
        xml.writeAttribute("missed", String.valueOf(counter.getMissedCount()));
        xml.writeAttribute("covered", String.valueOf(counter.getCoveredCount()));
    }

    private static void limpiarDirectorio(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ex) {
                        throw new RuntimeException("No se pudo limpiar " + path, ex);
                    }
                });
    }
}
