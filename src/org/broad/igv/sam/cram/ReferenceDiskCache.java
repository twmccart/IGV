package org.broad.igv.sam.cram;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import org.broad.igv.DirectoryManager;
import org.broad.igv.prefs.Constants;
import org.broad.igv.prefs.PreferencesManager;
import org.broad.igv.util.HttpUtils;

import java.io.*;
import java.util.zip.*;

/**
 * Created by jrobinso on 6/25/17.
 * <p>
 * Some static methods for managing the CRAM reference sequence cache
 */

public class ReferenceDiskCache {

    private static int MAX_SIZE = 1000000000;  // 1 GB,  make user preference?


    public static void saveSequence(String genomeId, String chr, byte[] bytes) throws IOException {

        File genomeDir = getGenomeDirectory(genomeId);

        FileOutputStream fos = null;

        try {
            File outputFile = new File(genomeDir, chr + ".bin");

            final FileOutputStream out = new FileOutputStream(outputFile);

            LittleEndianDataOutputStream dos = new LittleEndianDataOutputStream(out);
            dos.writeInt(bytes.length);

            DeflaterOutputStream gzipOutputStream = new DeflaterOutputStream(out);
            gzipOutputStream.write(bytes);
            gzipOutputStream.flush();
            gzipOutputStream.close();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    public static byte[] readSequence(String genomeId, String chr) throws IOException {

        File genomeDir = getGenomeDirectory(genomeId);
        File seqFile = new File(genomeDir, chr + ".bin");

        if(!seqFile.exists()) return null;

        FileInputStream fis = null;

        try {
            fis = new FileInputStream(seqFile);
            LittleEndianDataInputStream dis = new LittleEndianDataInputStream(fis);

            int size = dis.readInt();
            InflaterInputStream gis = new InflaterInputStream(fis);   //new ByteArrayInputStream(compressedBytes));
            byte [] buffer = new byte[size];
            HttpUtils.readFully(gis, buffer);

            return buffer;

        }
        finally {
            if(fis != null) fis.close();
        }


    }

    private static File getGenomeDirectory(String genomeId) {
        String rootDirectoryString = PreferencesManager.getPreferences().get(Constants.CRAM_SEQUENCE_DIRECTORY);

        File rootDirectory;
        if (rootDirectoryString != null) {
            rootDirectory = new File(rootDirectoryString);
        } else {
            rootDirectory = new File(DirectoryManager.getIgvDirectory(), "cram");
        }

        if (!rootDirectory.exists()) {
            rootDirectory.mkdir();
        }

        File genomeDir = new File(rootDirectory, genomeId);
        if (!genomeDir.exists()) {
            genomeDir.mkdir();
        }
        return genomeDir;
    }


}
