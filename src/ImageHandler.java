import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageHandler {
    public static void main(String[] args) throws Exception {
        ImageHandler p = new ImageHandler("src/pic1.jpg");
        var a = p.toImageByteArray();
        var list = p.getListOfImgPacket(128, a);
        System.out.println("Number of packets: " + list.size());
        for(byte[] l: list){
            System.out.println("Packet: " + Arrays.toString(l));
            System.out.println("Size of each packet: " + l.length);
        }

//        ByteArrayInputStream bis = new ByteArrayInputStream(data);
//        BufferedImage bImage2 = ImageIO.read(bis);
//        ImageIO.write(bImage2, "jpg", new File("output.jpg"));
//        System.out.println("image created");
    }

    private String img_path = null;

    public ImageHandler(String path){
        this.img_path = path;
    };

    public byte[] toImageByteArray(){
        BufferedImage bImage = null;
        try {

            bImage = ImageIO.read(new File(this.img_path));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(bImage, "jpg", bos);
            byte[] data = bos.toByteArray();
            System.out.println("The image has been converted into byte Array");
            return data;

        } catch (IOException e) {
            System.err.println("Could not find the image!"
                    + " Is it the right image path or filename?");
            throw new RuntimeException(e);
        }
    }

    public List<byte[]> getListOfImgPacket(int maxBuffSize, byte[] imgByteArray){
        int maxBuf = maxBuffSize - 24;
        List<byte[]> list = new ArrayList<>();
        int start = 0;
        while(start < imgByteArray.length){
            int end = Math.min(imgByteArray.length, start+maxBuf);
            list.add(Arrays.copyOfRange(imgByteArray, start, end));
            start += maxBuf;
        }

        return list;
    }

    public String getImg_path() {
        return this.img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }
}
