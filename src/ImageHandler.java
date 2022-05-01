import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageHandler {
    public static void main(String[] args) throws Exception {
        ImageHandler p = new ImageHandler("src/pic1.jpg");
        var a = p.toImageByteArray();
        System.out.println("image byte array size: " + a.length);
        var list = p.getListOfImgPacket(512, a);
        System.out.println("Number of packets: " + list.size());

        ByteBuffer buffer = ByteBuffer.allocate(1000);
        for(int i = 0; i < list.size(); i++){
            buffer.put(list.get(i));

            System.out.println("Packet: " + Arrays.toString(list.get(i)));
            //System.out.println("Packet in temp: " + Arrays.toString(temp));
            System.out.println("Size of each packet: " + list.get(i).length);
        }

        byte[] temp = buffer.array();


//        BufferedImage bImage = ImageIO.read(new File("src/inside.jpg"));
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ImageIO.write(bImage, "jpg", bos);
//        byte[] data = bos.toByteArray();

        System.out.println("combined: " + Arrays.toString(temp));
        ByteArrayInputStream bis = new ByteArrayInputStream(temp);
        BufferedImage bImage2 = ImageIO.read(bis);
        ImageIO.write(bImage2, "jpg", new File("output1.jpg"));
        System.out.println("image created");
    }

    private String img_path = null;
    private final List<byte[]> img_list;

    public ImageHandler(String path){
        this.img_path = path;
        this.img_list = new ArrayList<>();
    };

    public ImageHandler(List<byte[]> list){
        this.img_path = null;
        this.img_list = list;
    }
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

    public void Convert_toImage() {
        var list = this.img_list;
        if(!list.isEmpty()){
            ByteBuffer buffer = ByteBuffer.allocate(20000000);
            for (byte[] bytes : list) {
                buffer.put(bytes);
            }
            var temp = buffer.array();

            ByteArrayInputStream bis = new ByteArrayInputStream(temp);
            try {
                BufferedImage bImage2 = ImageIO.read(bis);
                ImageIO.write(bImage2, "jpg", new File("received.jpg"));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            System.out.println("There is no data packets of image to convert back to image!");
        }


    }

    public String getImg_path() {
        return this.img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }
}
