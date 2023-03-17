from PIL import Image
from PIL import ImageFile
import os
import sys
import shutil
from loguru import logger
import optparse
import re


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
# 判断是否是数字
def isNumber(string):
    pattern = re.compile(r'^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$')
    return bool(pattern.match(string))

def unitConversion(imageSize):
    if imageSize.endswith("kb") or imageSize.endswith("KB"):
        return int(imageSize[:-2])
    if imageSize.endswith("m") or imageSize.endswith("M"):
        return int(imageSize[:-1])*1024
    if imageSize.endswith("g") or imageSize.endswith("G"):
        return int(imageSize[:-1])*1024*1024

@logger.catch
def isPicture(fileName):
    if (fileName.lower().endswith(('.bmp', '.dib', '.png', '.jpg', '.jpeg', '.pbm', '.pgm', '.ppm', '.tif', '.tiff'))):
        return True
    else:
        return False

@logger.catch
def singlePictureCompress(filePath,isRaw,outPut,imageSize=1024):
    if outPut==None:
        outPut = filePath
    # if os.path.exists(filePath + ".raw"):
    #     shutil.copy(filePath+".raw",filePath)
    if isRaw:
        if os.path.exists(filePath + ".raw"):
            shutil.copy(filePath+".raw", outPut)
        elif filePath.endswith(".raw"):
            print(filePath[:-4])
            shutil.copy(filePath[:-4],outPut)
    if not (filePath.endswith(".raw") or os.path.exists(filePath + ".raw")):
        shutil.copy(filePath,filePath+".raw")
    compress_image(outPut,imageSize)

if __name__ == '__main__':

    logger.add('./logs/scaleImage{time:YYYY-MM-DD}.log',

               level='DEBUG',

               format='{time:YYYY-MM-DD HH:mm:ss} - {level} - {file} - {line} - {message}',

               rotation="00:00", retention="30 days")

    usage = "Usage: %prog [options] arg1 arg2 ..."
    parser = optparse.OptionParser(usage, version="%prog 1.0")

    # 自定义可以解析的参数名
    parser.add_option("-f", "--file", action="store", dest="filename", type="string", metavar="FILE",
                      help="target file")
    parser.add_option("-d", "--dir", action="store", dest="dirPath", type="string", metavar="directory",
                      help="target directory")

    parser.add_option("-s", "--size", action="store", dest="size", type="string", metavar="imageSize",
                      help="limit image size,default 1024k")

    parser.add_option("-r","--raw",action="store_true",dest="raw",help="Output target to path")

    parser.add_option("-o", "--output", action="store", dest="outputPath", type="string", metavar="outputPath",
                      help="Output target to path")

    options, args = parser.parse_args()
    print(options,args)
    if options.filename==None and options.dirPath==None:
        parser.error("file or dir must select one")
        parser.exit()
    if options.filename!=None and options.dirPath!=None:
        parser.error("file and dir,Only one can be selected")
        parser.exit()

    imageSize = 1024
    if options.size:
        if isNumber(options.size):
            imageSize =options.size
        elif options.size.endswith("g") or options.size.endswith("G") or options.size.endswith("m") or options.size.endswith("M"):
            imageSize = unitConversion(options.size)
        else:
            parser.error("Image size is an incorrect input")

    if options.filename:
        outPut = options.outputPath
        if options.outputPath:
            if not os.path.exists(options.outputPath):
                os.makedirs(options.outputPath)
            fileName = os.path.basename(options.filename)
            outPut = os.path.join(options.outputPath, fileName)
            shutil.copy(options.filename,outPut)
        singlePictureCompress(options.filename,options.raw,outPut,imageSize)
    if options.dirPath:
        outPut = options.outputPath
        if options.outputPath:
            if not os.path.exists(options.outputPath):
                os.makedirs(options.outputPath)

        if os.path.exists(options.dirPath):
            # root 所指的是当前正在遍历的这个文件夹的本身的地址
            # dirs 是一个 list，内容是该文件夹中所有的目录的名字(不包括子目录)
            # files 同样是 list, 内容是该文件夹中所有的文件(不包括子目录)
            for root, dirs, files in os.walk(options.dirPath):
                for file in files:
                    src_file = os.path.join(root, file)
                    if options.raw and (isPicture(src_file) or src_file.endswith(".raw")):
                        logger.info("copy file is " + src_file)
                        if options.outputPath:
                            shutil.copy(src_file, options.outputPath)
                            outPut = os.path.join(options.outputPath, file)

                        singlePictureCompress(src_file, options.raw, outPut, imageSize)
                    elif isPicture(src_file):
                        if options.outputPath:
                            shutil.copy(src_file, options.outputPath)
                            outPut = os.path.join(options.outputPath, file)
                        singlePictureCompress(src_file, options.raw, outPut, imageSize)



