import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Wget {
    public interface ProgressListener{
        void progressUpdate(long progress, long expected);
    }

    public static void main(String[] args) {
        if(args==null || args.length==0){
            System.out.println("Usage:");
            System.out.println("  wget <url>");
            return;
        }

        String url = args[0];
        String dest = "index.html";
        if(url.startsWith("http://")){
            url = url.substring("http://".length(), url.length());
        }
        if(url.contains("/")){
            int index = url.lastIndexOf('/');
            dest = url.substring(index+1, url.length());
        }

        url = "http://"+url;

        Wget wget = new Wget();
        try {
            wget.getFile(url, dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getFile(String url, String dest) throws IOException {
        URL url1 = null;
        HttpURLConnection connection = null;
        try {
            url1 = new URL(url);
            connection = (HttpURLConnection) url1.openConnection();
            connection.setConnectTimeout(2 * 1000);
            connection.setReadTimeout(2 * 1000);
            connection.connect();

            File temp = new File(dest);
            if (!temp.exists()) {
                temp.createNewFile();
            }

            int contentLength = connection.getContentLength();

            // write file
            writeToFile(connection, dest, new ProgressListener() {
                @Override
                public void progressUpdate(long progress, long expected) {
                    if(contentLength>0) {
                        int percent = (int) (((double) progress / (double) contentLength) * 100);
                        System.out.print(String.format("[%d%%] %d/%d\r", percent, progress, contentLength));
                    }
                }
                long contentLength = 0;

                public ProgressListener setContentLength(long length){
                    this.contentLength = length;
                    return this;
                }
            }.setContentLength(contentLength));

        } catch (IOException e) {
            System.out.println("error geturl:" + url);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void writeToFile(HttpURLConnection connection, String dest, ProgressListener progress) throws IOException {
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(dest));
        try {
            long total=0;
            InputStream is = connection.getInputStream();
            byte[] buff = new byte[1024];
            int length = 0;
            while ((length = is.read(buff, 0, 1024)) != -1) {
                total += length;
                fos.write(buff, 0, length);

                if(progress!=null){
                    progress.progressUpdate(total, 0);
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
}
