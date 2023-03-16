from PIL import Image
from PIL import ImageFile
import os
import sys
import shutil
from loguru import logger


# 压缩图片文件
@logger.catch
def compress_image(outfile, mb=1024, quality=85, k=0.9):  # 通常你只需要修改mb大小
    """不改变图片尺寸压缩到指定大小
    :param outfile: 压缩文件保存地址
    :param mb: 压缩目标，KB
    :param k: 每次调整的压缩比率
    :param quality: 初始压缩比率
    :return: 压缩文件地址，压缩文件大小
    """
    if not isPicture(outfile):
        print(outfile,"is not picture")
        return


    o_size = os.path.getsize(outfile) // 1024  # 函数返回为字节，除1024转为kb（1kb = 1024 bit）
    print('before_size:{} after_size:{}'.format(o_size, mb))
    if o_size <= mb:
        return outfile

    logger.info("compress image " + outfile)

    ImageFile.LOAD_TRUNCATED_IMAGES = True  # 防止图像被截断而报错

    while o_size > mb:
        im = Image.open(outfile)
        x, y = im.size
        out = im.resize((int(x * k), int(y * k)), Image.ANTIALIAS)  # 最后一个参数设置可以提高图片转换后的质量
        try:
            out.save(outfile, quality=quality)  # quality为保存的质量，从1（最差）到95（最好），此时为85
        except Exception as e:
            print(e)
            break
        o_size = os.path.getsize(outfile) // 1024
    return outfile

@logger.catch
def getDirFiles(path):
    pictureFiles=[]
    if os.path.exists(path):
        # root 所指的是当前正在遍历的这个文件夹的本身的地址
        # dirs 是一个 list，内容是该文件夹中所有的目录的名字(不包括子目录)
        # files 同样是 list, 内容是该文件夹中所有的文件(不包括子目录)
        for root, dirs, files in os.walk(path):
            for file in files:
                src_file = os.path.join(root, file)
                if isPicture(src_file):
                    pictureFiles.append(src_file)
    return pictureFiles

@logger.catch
def copyDir(source_path,target_path):
    if not os.path.exists(target_path):
        os.makedirs(target_path)
    if os.path.exists(source_path):
        # root 所指的是当前正在遍历的这个文件夹的本身的地址
        # dirs 是一个 list，内容是该文件夹中所有的目录的名字(不包括子目录)
        # files 同样是 list, 内容是该文件夹中所有的文件(不包括子目录)
        for root, dirs, files in os.walk(source_path):
            for file in files:
                src_file = os.path.join(root, file)
                if isPicture(src_file):
                    logger.info("copy file is "+ src_file)
                    shutil.copy(src_file, target_path)


@logger.catch
def isPicture(fileName):
    if (fileName.lower().endswith(('.bmp', '.dib', '.png', '.jpg', '.jpeg', '.pbm', '.pgm', '.ppm', '.tif', '.tiff'))):
        return True
    else:
        return False


if __name__ == '__main__':

    logger.add('./logs/scaleImage{time:YYYY-MM-DD}.log',

               level='DEBUG',

               format='{time:YYYY-MM-DD HH:mm:ss} - {level} - {file} - {line} - {message}',

               rotation="00:00", retention="30 days")


    argv = sys.argv
    if len(argv) < 4:
        logger.info("python3 scaleImage.py [-f|-d] [inputFile|inputDir] [outputFile|outputDir] [imageSize]")
        print("python3 scaleImage.py [-f|-d] [inputFile|inputDir] [outputFile|outputDir] [imageSize]")
        sys.exit()
    if argv[1] != "-d" and argv[1] != "-f":
        logger.info("python3 scaleImage.py [-f|-d] [inputFile|inputDir] [outputFile|outputDir] [imageSize]")
        print("python3 scaleImage.py [-f|-d] [inputFile|inputDir] [outputFile|outputDir] [imageSize]")
        sys.exit()

    inputFile,outputFile = argv[2],argv[3]

    logger.info("inputFile is "+inputFile)

    logger.info("inputFile is " + outputFile)

    imageSize = 1024
    if len(argv)>=5:
        imageSize = argv[4]
    if argv[1]=="-f":
        if not os.path.isfile(inputFile):
            logger.info("file is not find")
            print("file is not find")
        logger.info("copy file is " + inputFile)
        shutil.copy(inputFile,outputFile)
        compress_image(outputFile,imageSize)

    if argv[1]=="-d":
        if not os.path.isdir(inputFile):
            logger.info("dir is not find")
            print("dir is not find")
        copyDir(inputFile,outputFile)
        files = getDirFiles(outputFile)
        for file in files:
            compress_image(file,imageSize)


