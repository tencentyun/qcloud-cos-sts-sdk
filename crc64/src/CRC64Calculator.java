import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CRC64Calculator {
    // ECMA-182 标准参数
    private static final long POLY = 0xC96C5795D7870F42L;
    private static final long INIT = 0xFFFFFFFFFFFFFFFFL;
    private static final long XOR_OUT = 0xFFFFFFFFFFFFFFFFL;
    private static final int GF2_DIM = 64;

    // 预计算表 (256 entries)
    private static final long[] crcTable = generateTable();

    private long crc;

    public CRC64Calculator() {
        reset();
    }

    // 生成预计算表
    private static long[] generateTable() {
        long[] table = new long[256];
        for (int i = 0; i < 256; i++) {
            long crc = i;
            for (int j = 0; j < 8; j++) {
                if ((crc & 1) != 0) {
                    crc = (crc >>> 1) ^ POLY;
                } else {
                    crc >>>= 1;
                }
            }
            table[i] = crc;
        }
        return table;
    }

    // 重置计算器
    public void reset() {
        crc = INIT;
    }

    // 更新数据块
    public void update(byte[] data) {
        update(data, 0, data.length);
    }

    public void update(byte[] data, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            int index = (int) ((crc ^ (data[i] & 0xFF)) & 0xFF);
            crc = (crc >>> 8) ^ crcTable[index];
        }
    }

    // 获取最终结果
    public long getVlue() {
        return crc ^ XOR_OUT;
    }

    // GF(2) 矩阵运算
    private static long gf2MatrixTimes(long[] mat, long vec) {
        long sum = 0;
        int index = 0;
        while (vec != 0) {
            if ((vec & 1) != 0) {
                sum ^= mat[index];
            }
            vec >>>= 1;
            index++;
        }
        return sum;
    }

    private static void gf2MatrixSquare(long[] square, long[] mat) {
        for (int n = 0; n < GF2_DIM; n++) {
            square[n] = gf2MatrixTimes(mat, mat[n]);
        }
    }

    // 合并两个 CRC 值
    public static long combine(long crc1, long crc2, long len2) {
        if (len2 == 0) return crc1;

        // 调整初始状态
        crc1 ^= (INIT ^ XOR_OUT);

        // 初始化 GF(2) 矩阵
        long[] even = new long[GF2_DIM];
        long[] odd = new long[GF2_DIM];

        // 构建反转多项式矩阵
        odd[0] = POLY;
        long row = 1;
        for (int n = 1; n < GF2_DIM; n++) {
            odd[n] = row;
            row <<= 1;
        }

        // 矩阵平方运算
        gf2MatrixSquare(even, odd);
        gf2MatrixSquare(odd, even);

        // 合并运算
        while (true) {
            gf2MatrixSquare(even, odd);
            if ((len2 & 1) != 0) {
                crc1 = gf2MatrixTimes(even, crc1);
            }
            len2 >>>= 1;
            if (len2 == 0) break;

            gf2MatrixSquare(odd, even);
            if ((len2 & 1) != 0) {
                crc1 = gf2MatrixTimes(odd, crc1);
            }
            len2 >>>= 1;
            if (len2 == 0) break;
        }

        return (crc1 ^ crc2);
    }

    // 文件校验
    public static long fileCRC(String path) throws IOException {
        CRC64Calculator crc = new CRC64Calculator();
        try (FileInputStream fis = new FileInputStream(path)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
        }
        return crc.getVlue();
    }

    public static void main(String[] args) {
        // 测试用例
        testString("123456789");
        testString("中文");
        testStream();
        testCombine();

        try {
            System.out.println("文件校验: " + fileCRC("image.png"));
        } catch (IOException e) {
            System.err.println("文件读取错误: " + e.getMessage());
        }
    }

    private static void testString(String str) {
        CRC64Calculator crc = new CRC64Calculator();
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        crc.update(data);
        System.out.printf("%s: %s\n", str, Long.toUnsignedString(crc.getVlue(),10));
    }

    private static void testStream() {
        CRC64Calculator crc = new CRC64Calculator();
        crc.update("123456".getBytes(StandardCharsets.UTF_8));
        crc.update("789".getBytes(StandardCharsets.UTF_8));
        System.out.println("流式校验123456789: " + Long.toUnsignedString(crc.getVlue(),10));
    }

    private static void testCombine() {
        CRC64Calculator part1 = new CRC64Calculator();
        part1.update("123456".getBytes(StandardCharsets.UTF_8));
        long crc1 = part1.getVlue();

        CRC64Calculator part2 = new CRC64Calculator();
        part2.update("789".getBytes(StandardCharsets.UTF_8));
        long crc2 = part2.getVlue();

        long combined = combine(crc1, crc2, 3);
        System.out.println("合并结果: " + Long.toUnsignedString(combined,10));
    }
}