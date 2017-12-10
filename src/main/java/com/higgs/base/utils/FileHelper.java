package com.higgs.base.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipInputStream;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import com.higgs.base.constants.ExtConstants;

/**
 * 文件处理帮助类
 * @author terry
 *
 */
public class FileHelper extends FileUtils
{
    
    public static void closeStream(InputStream in,OutputStream out)
    {
        try
        {
            if (in != null)
            {
                in.close();
            }
            if (out != null)
            {
                out.close();
            }
        }
        catch (IOException ex)
        {
            LogHelper.logError(ex, "关闭流失败，请检查！");
        }
    }
    
    public static void closeStream(Reader reader,Writer writer)
    {
        try
        {
            if (reader != null)
            {
                reader.close();
            }
            if (writer != null)
            {
                writer.close();
            }
        }
        catch (IOException ex)
        {
            LogHelper.logError(ex, "关闭流失败，请检查！");
        }
    }
    
    /**
     * 获取文件大小
     * 
     * @param src 目标文件
     * @return
     */
    public static String getFileSize4Format(File file)
    {
        long byteSize = file.length();
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSize = "";
        
        if (byteSize < 1024)
        {
            fileSize = df.format((double)byteSize) + "Byte";
        }
        else if (byteSize < Math.pow(1024, 2))
        {
            fileSize = df.format((double)byteSize / 1024) + "KB";
        }
        else if (byteSize < Math.pow(1024, 3))
        {
            fileSize = df.format(byteSize / Math.pow(1024, 2)) + "MB";
        }
        else if (byteSize < Math.pow(1024, 4))
        {
            fileSize = df.format(byteSize / Math.pow(1024, 3)) + "GB";
        }
        else
        {
            fileSize = df.format(byteSize / Math.pow(1024, 4)) + "TB";
        }
        return fileSize;
    }
    
    /**
     * 快速读取文件内容，转成字符串。注意：请不要使用该方法读取大型文件，该方法会一次性将文件读取至内存
     * 
     * @param filePath 文件路径
     * @param encode 默认utf-8
     * @return 文件内容
     * @throws IOException
     */
    public static String readFile2Str4Quickly(String filePath,String encode) throws IOException
    {
        if (StringHelper.isEmpty(encode))
        {
            encode = ExtConstants.DEF_ENCODE;
        }
        
        byte[] fileStream = readFile4Quickly(new File(filePath));
        String context = new String(fileStream, encode);
        
        return context;
    }
    
    /**
     * 快速读取文件2进制流，转成字符串。注意：请不要使用该方法读取大型文件，该方法会一次性将文件读取至内存
     * 
     * @param file 文件
     * @return 文件2进制流
     * @throws IOException
     */
    public static byte[] readFile4Quickly(File file) throws IOException
    {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeData(in, out);
        return out.toByteArray();
    }
    
    /**
     * 快速写出字符串内容至目的文件
     * 
     * @param filePath 文件路径
     * @param encode 默认utf-8
     * @return 文件内容
     */
    public static void writeFile4Quickly(String destPath,String fileContext,String encode)
    {
        if (StringHelper.isEmpty(encode))
        {
            encode = ExtConstants.DEF_ENCODE;
        }
        OutputStream out = null;
        try
        {
            File destFile = new File(destPath);
            if (!destFile.getParentFile().exists())
                destFile.getParentFile().mkdirs();
            
            out = new BufferedOutputStream(new FileOutputStream(destPath));
            out.write(fileContext.getBytes(encode));
            out.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeStream(null, out);
        }
    }
    
    /**
     * 快速文件拷贝，该方法仅限于小文件的拷贝，请勿用于大型文件拷贝(超过100MB)
     * 
     * @param srcPath 源文件路径
     * @param destPath 目的文件路径
     */
    public static void copyFile4Quickly(String srcPath,String destPath)
    {
        InputStream in = null;
        ByteArrayOutputStream baos = null;
        OutputStream out = null;
        byte[] buffer = new byte[ExtConstants.DEF_BUFFER_SIZE];
        try
        {
            in = new BufferedInputStream(new FileInputStream(srcPath));
            baos = new ByteArrayOutputStream();
            int len;
            while ((len = in.read(buffer)) > 0)
            {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            
            out = new BufferedOutputStream(new FileOutputStream(destPath));
            out.write(baos.toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeStream(in, out);
            closeStream(null, baos);
        }
    }
    
    /**
     * 分割文件成指定分块数量的子文件
     * 
     * @param file 目标文件
     * @param partNum 子文件个数
     * @return 分割后的文件集合
     * @throws IOException
     */
    public static List<File> splitFile4Part(File file,int partNum) throws IOException
    {
        if (file == null || !file.exists() || file.isDirectory())
        {
            return new ArrayList<File>();
        }
        
        // 计算每个部分的长度
        long fileLen = file.length();
        long partLength = (fileLen + partNum - 1) / partNum;
        return splitFile4Size(file, partLength);
    }
    
    /**
     * 按照指定的分块大小，将分割文件成若干个子文件
     * 
     * @param file 目标文件
     * @param partLength 块大小
     * @return 分割后的文件集合
     * @throws IOException
     */
    public static List<File> splitFile4Size(File file,long partLength) throws IOException
    {
        List<File> subFiles = new ArrayList<File>();
        
        if (file == null || !file.exists() || file.isDirectory())
        {
            return subFiles;
        }
        
        // 获取文件名和对应的后缀
        String fileName = file.getName();
        String pureFileName = StringHelper.substringBeforeLast(fileName, ".");
        String suffix = StringHelper.substringAfterLast(fileName, ".");
        
        // 创建一个同名的文件夹
        String dirName = "_" + fileName;
        String dirPath = file.getParent() + "/" + dirName;
        File dir = new File(dirPath);
        
        // 如果目录存在，则清空目录中内容，否则新建目录
        if (dir.exists())
        {
            clearDirectory(dir);
        }
        else
        {
            dir.mkdir();
        }
        
        long bufferSize = ExtConstants.DEF_BUFFER_SIZE;
        long readLen = 0; // 以读取的长度
        int len;
        int currentPart = 1;
        
        byte[] defaultBuffer = new byte[(int)bufferSize]; // 默认缓冲区
        byte[] buffer = defaultBuffer;
        
        // 计算使用缓冲区时，每一个子块总的读取次数
        long totalTimes = partLength % bufferSize == 0 ? partLength / bufferSize : partLength / bufferSize + 1;
        long currentTimes = 0; // 当前读取次数
        
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try
        {
            File subFile = new File(dirPath + "/" + pureFileName + "_" + currentPart + "." + suffix);
            bis = new BufferedInputStream(new FileInputStream(file));
            bos = new BufferedOutputStream(new FileOutputStream(subFile));
            
            while ((len = bis.read(buffer)) > 0)
            {
                readLen += len; // 已读长度
                bos.write(buffer, 0, len);
                
                // 如果下一次是当前部分的最后一次读取，使用本分块的剩余长度作为缓冲区大小
                if (++currentTimes == totalTimes - 1)
                {
                    buffer = new byte[(int)(partLength - readLen)];
                }
                
                // 如果已经读完当前部分的所有长度，重置变量，指向新的文件输出流
                if (readLen == partLength)
                {
                    currentTimes = 0;
                    readLen = 0;
                    buffer = defaultBuffer;
                    bos.close();
                    subFiles.add(subFile);
                    subFile = new File(dirPath + "/" + pureFileName + "_" + ++currentPart + "." + suffix);
                    bos = new BufferedOutputStream(new FileOutputStream(subFile));
                }
            }
            subFiles.add(subFile); // 将最后一个子文件，添加至集合
        }
        finally
        {
            closeStream(bis, bos);
        }
        return subFiles;
    }
    
    /**
     * 将指定目录下的被分割的块文件，合并成完整的文件
     * 
     * @param dir
     * @param fileComparetor
     * @return
     * @throws IOException
     */
    public static File uniteApartFilesByDirectory(File dir,Comparator<File> fileComparetor) throws IOException
    {
        if (dir == null || dir.isFile() || fileComparetor == null)
        {
            return null;
        }
        
        // 获取合并后的文件名，生成的文件在目录的父级路径下
        String uniteFilePath = dir.getParent() + "/_" + dir.getName();
        File uniteFile = new File(uniteFilePath);
        
        // 获取目录下的所有文件，排列文件的拼装顺序
        File[] subFiles = dir.listFiles();
        TreeSet<File> fileSet = new TreeSet<File>(fileComparetor);
        for (File subFile : subFiles)
        {
            fileSet.add(subFile);
        }
        
        List<File> subFileList = new ArrayList<File>();
        subFileList.addAll(fileSet);
        
        uniteApartFiles(uniteFile, subFileList);
        return uniteFile;
    }
    
    /**
     * 将被分割的块文件，合并成完整的文件
     * 
     * @param uniteFile 目标合并后的文件
     * @param partFiles 文件块集合
     * @throws IOException
     */
    public static void uniteApartFiles(File uniteFile,List<File> partFiles) throws IOException
    {
        // 如果存在相同文件时，重名时的名称
        SequenceInputStream sis = null;
        OutputStream bos = null;
        
        if (uniteFile.exists())
        {
            String uniteFilePath = uniteFile.getParent() + "/_unite_" + uniteFile.getName();
            uniteFile = new File(uniteFilePath);
        }
        try
        {
            bos = new BufferedOutputStream(new FileOutputStream(uniteFile));
            
            // 连接每个块文件，生成顺序流
            List<InputStream> insList = new ArrayList<InputStream>();
            for (File partFile : partFiles)
            {
                InputStream ins = new BufferedInputStream(new FileInputStream(partFile));
                insList.add(ins);
            }
            
            sis = new SequenceInputStream(Collections.enumeration(insList));
            writeData(sis, bos);
        }
        finally
        {
            FileHelper.closeStream(sis, bos);
        }
    }
    
    /**
     * IO底层方法：输出数据
     * 
     * @param in 输入流
     * @param out 输出流
     * @throws IOException
     */
    public static void writeData(InputStream in,OutputStream out) throws IOException
    {
        byte[] buffer = new byte[ExtConstants.DEF_BUFFER_SIZE];
        int len;
        try
        {
            while ((len = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, len);
            }
        }
        finally
        {
            closeStream(in, out);
        }
    }
    
    /**
     * 清空文件夹下所有内容
     * 
     * @param dir 文件夹目录
     */
    public static void clearDirectory(File dir)
    {
        // 如果不存在该目录，则创建
        if (!dir.exists())
        {
            dir.mkdir();
            return;
        }
        
        // 如果该地址为单一文件，不是一个文件夹，则退出
        if (dir.isFile())
        {
            return;
        }
        
        // 获取文件夹下所有的文件，遍历删除
        File[] subFiles = dir.listFiles();
        for (File subFile : subFiles)
        {
            // 如果子文件也是文件夹，递归删除
            if (subFile.isDirectory())
            {
                clearDirectory(subFile);
                subFile.delete();
            }
            else
            {
                subFile.delete();
            }
        }
    }
    
    /**
     * 删除文件或者文件夹
     * 
     * @param file
     */
    public static void deleteFileOrDir(File file)
    {
        if (!file.exists())
        {
            return;
        }
        
        if (file.isFile())
        {
            file.delete();
        }
        else
        {
            // 如果是文件夹，先清理文件夹，然后删除文件夹
            clearDirectory(file);
            file.delete();
        }
    }
    
    /**
     * 获取文件名的细节内容，[0] = 不带后缀的纯文件名，[1] = 文件后缀，如果无后缀，为""
     * 
     * @param file 目标文件
     * @return 细节内容
     */
    public static String[] getFileNameDetails(File file)
    {
        String fileName = file.getName();
        String pureName = StringHelper.substringBeforeLast(fileName, ".");
        String suffix = StringHelper.toStrWithDef(StringHelper.substringAfterLast(fileName, "."), "");
        return new String[] {pureName, suffix};
    }
    
    /**
     * 根据指定索引位置，分割文件
     * 
     * @param file 目标文件
     * @param indexs 索引位置集合[0] = startIndex,[1] =endIndex
     * @throws IOException
     */
    public static void splitFileByPostition(File file,List<Long[]> indexs) throws IOException
    {
        
        File tempDir = new File(file.getParent() + "/_" + file.getName());
        if (tempDir.exists())
            clearDirectory(tempDir);// 如果之前分割后，需要进行重新分割，则清理临时分割文件夹
        else
            tempDir.mkdir();
        
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        BufferedOutputStream bos = null;
        int bufferSize = 8192; // 默认缓冲区大小
        byte[] defBuffer = new byte[bufferSize]; // 默认缓冲区
        byte[] buffer = defBuffer;
        int seq = 1; // 分割块序号
        long startIndex; // 起始索引点
        long endIndex; // 终止索引点
        long restLen; // 剩余长度
        int readLen; // 已读长度
        
        try
        {
            for (Long[] indexArr : indexs)
            {
                bos = new BufferedOutputStream(new FileOutputStream(new File(tempDir.getAbsolutePath() + "/" + seq++)));
                startIndex = indexArr[0];
                endIndex = indexArr[1];
                restLen = endIndex - startIndex + 1;
                while (restLen != 0)
                {
                    readLen = bis.read(buffer);
                    bos.write(buffer, 0, readLen);
                    
                    restLen += -1 * readLen;
                    buffer = (restLen < bufferSize && restLen != 0) ? new byte[(int)restLen] : defBuffer;
                }
                bos.close();
            }
        }
        finally
        {
            closeStream(bis, bos);
        }
    }
    
    public static void copyFileByBuffer(File srcFile,File cpoyFile) throws Exception
    {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cpoyFile));
        byte[] buffer = new byte[8192];
        int len = 0;
        while ((len = bis.read(buffer)) > 0)
        {
            bos.write(buffer, 0, len);
        }
        bos.flush();
    }
    
    public static void copyFileByMap(File srcFile,File cpoyFile) throws Exception
    {
        RandomAccessFile rafi = new RandomAccessFile(srcFile, "r");
        RandomAccessFile rafo = new RandomAccessFile(cpoyFile, "rw");
        
        FileChannel fic = rafi.getChannel();
        FileChannel foc = rafo.getChannel();
        
        long fileSize = srcFile.length();
        MappedByteBuffer inBuffer = fic.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
        MappedByteBuffer outBuffer = foc.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
        
        for (int i = 0; i < fileSize; i++)
        {
            byte b = inBuffer.get(i);
            outBuffer.put(i, b);
        }
        fic.close();
        foc.close();
    }
    
    /**
     * 将所有源文件对象进行压缩打包，生成对应的压缩文件<br/>
     * 注意：该方法不同于常规的打包，所有目标文件不保留目录层级关系，所有文件压缩后，均放在同一级目录下
     * 
     * @param srcFiles 源文件，如果源文件内部存在目录，目录会被忽略，目录下的文件会被加入打包
     * @param zipFilePath 压缩文件地址
     * @param encode 压缩时使用编码，默认utf-8，windows下，utf-8会使中文文件名变乱码，可使用gbk打包
     * 
     */
    public static void zipFiles(List<File> srcFiles,File zipFile,String encode)
    {
        if (StringHelper.isEmpty(encode))
            encode = "utf-8";
        
        if (!zipFile.getParentFile().exists())
            zipFile.getParentFile().mkdirs();
        
        // 创建压缩输出流，处理压缩文件
        ZipOutputStream zos = null;
        
        try
        {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
//            zos.setEncoding(encode);
            
            for (File subFile : srcFiles)
                handleZip(zos, subFile);
        }
        catch (Exception ex)
        {
            LogHelper.logError(ex, "压缩文件时出现异常！");
        }
        finally
        {
            FileHelper.closeStream(null, zos);
        }
    }
    
    /**
     * 将资源文件添加至压缩流中
     * 
     * @param zos
     * @param srcFile
     * @throws IOException
     */
    public static void handleZip(ZipOutputStream zos,File srcFile) throws IOException
    {
        InputStream in = null;
        byte[] buffer = new byte[8192];
        
        // 如果目标为文件，加入压缩流
        if (srcFile.isFile())
        {
            try
            {
                in = new BufferedInputStream(new FileInputStream(srcFile));
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int length = 0;
                while ((length = in.read(buffer)) > 0)
                    zos.write(buffer, 0, length);
                
                zos.closeEntry();
            }
            finally
            {
                FileHelper.closeStream(in, null);
            }
        }
        // 如果目标为文件夹目录，遍历下属的文件，进行压缩
        else
        {
            File[] files = srcFile.listFiles();
            for (File subFile : files)
                handleZip(zos, subFile);
        }
    }
    
    public static void handleZip(ZipOutputStream zos,String ZipEntryName,InputStream in)
    {
        try
        {
            zos.putNextEntry(new ZipEntry(ZipEntryName));
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0)
            {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
        catch (IOException e)
        {
            LogHelper.logError(e, "进行文件打包压缩时出现异常！");
        }
    }
    
    /**
     * 功能：把 sourceDir 目录下的所有文件进行 zip 格式的压缩，保存为指定 zip 文件
     * 
     * @param sourceDir
     * @param zipFile
     */
    public static void zipDir(String sourceDir,String zipFile,String encode)
    {
        if (StringHelper.isEmpty(encode))
            encode = "utf-8";
        
        ZipOutputStream zos = null;
        
        try
        {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
//            zos.setEncoding(encode);
            File file = new File(sourceDir);
            
            String basePath = null;
            
            if (file.isDirectory())
                basePath = file.getPath();
            else
                basePath = file.getParent();
            
            handleZip(file, basePath, zos);
        }
        catch (Exception ex)
        {
            LogHelper.logError(ex, "压缩文件时出现异常！");
        }
        finally
        {
            FileHelper.closeStream(null, zos);
        }
        
    }
    
    /**
     * 功能：执行文件压缩成zip文件
     * 
     * @param source
     * @param basePath 待压缩文件根目录
     * @param zos
     * @throws IOException
     */
    private static void handleZip(File source,String basePath,ZipOutputStream zos) throws IOException
    {
        
        File[] files = new File[0];
        
        if (source.isDirectory())
            files = source.listFiles();
        else
        {
            files = new File[1];
            files[0] = source;
        }
        
        String pathName;// 存相对路径(相对于待压缩的根目录)
        byte[] buf = new byte[8192];
        int length = 0;
        BufferedInputStream bis = null;
        try
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    pathName = file.getPath().substring(basePath.length() + 1) + "/";
                    zos.putNextEntry(new ZipEntry(pathName));
                    handleZip(file, basePath, zos);
                }
                else
                {
                    pathName = file.getPath().substring(basePath.length() + 1);
                    bis = new BufferedInputStream(new FileInputStream(file));
                    zos.putNextEntry(new ZipEntry(pathName));
                    
                    while ((length = bis.read(buf)) > 0)
                        zos.write(buf, 0, length);
                    
                    bis.close();
                }
            }
        }
        finally
        {
            FileHelper.closeStream(bis, null);
        }
    }
    
    public static void unZipFile(File zipFile,File destDir)
    {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        ZipFile zip = null;
        try
        {
            zip = new ZipFile(zipFile);
            Enumeration<?> zipEntries = zip.getEntries();
            while (zipEntries.hasMoreElements())
            {
                ZipEntry zipEntry = (ZipEntry)zipEntries.nextElement();
                
                String destDirPath = destDir.getAbsolutePath();
                File destFile = new File(destDirPath + "/" + zipEntry.getName());
                
                if (zipEntry.isDirectory())
                    destFile.mkdirs();
                
                else
                {
                    if (!destFile.getParentFile().exists())
                        destFile.getParentFile().mkdirs();
                    
                    bos = new BufferedOutputStream(new FileOutputStream(destFile));
                    bis = new BufferedInputStream(zip.getInputStream(zipEntry));
                    int length;
                    byte[] buf = new byte[8192];
                    while ((length = bis.read(buf)) > 0)
                        bos.write(buf, 0, length);
                }
            }
        }
        catch (IOException ex)
        {
            LogHelper.logError(ex, "解压文件时出现异常！");
        }
        finally
        {
            FileHelper.closeStream(bis, bos);
            if (zip != null)
            {
                try
                {
                    zip.close();
                }
                catch (IOException ex)
                {
                    LogHelper.logError(ex, "关闭压缩文件流时出现异常");
                }
            }
        }
    }
    
    public static byte[] zipData(byte[] data)
    {
        byte[] b = null;
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            
            ZipOutputStream zip = new ZipOutputStream(bos);
            ZipEntry entry = new ZipEntry("zip");
            
            entry.setSize(data.length);
            zip.putNextEntry(entry);
            zip.write(data);
            zip.closeEntry();
            zip.close();
            b = bos.toByteArray();
            bos.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return b;
        
    }
    
    public static byte[] unZipData(byte[] data)
    {
        byte[] b = null;
        try
        {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ZipInputStream zip = new ZipInputStream(bis);
            
            while (zip.getNextEntry() != null)
            {
                byte[] buf = new byte[8192];
                
                int length = -1;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                while ((length = zip.read(buf, 0, buf.length)) != -1)
                {
                    baos.write(buf, 0, length);
                }
                
                b = baos.toByteArray();
                baos.flush();
                baos.close();
            }
            
            zip.close();
            bis.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return b;
    }
    
    public static void main(String[] args)
    {
        BufferedReader br = null;
        BufferedWriter bw = null;
        String charSet = "gbk";
        String deskTopPath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath(); // 获得桌面路径
        String filePath = deskTopPath + "/111.txt";
        
        try
        {
            // 此处，要用FileInputStream 来代替 FileReader，该构造函数可以设置编码，否则会出现乱码问题 
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath)), charSet));
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(deskTopPath + "/destReverse.txt")), charSet));
            
            /* 1.读取源文本中的每一行，存放在集合中*/
            List<String> contextList = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null)
                contextList.add(line);
            
            /* 2. 反转集合，写出到目标文件 */
            for (int index = contextList.size() - 1; index >= 0; index--)
            {
                line = contextList.get(index);
                bw.write(line);
                bw.newLine();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(); 
        }
        finally
        {
            try
            {
                if (bw != null)
                    bw.close();
                
                if (br != null)
                    br.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
