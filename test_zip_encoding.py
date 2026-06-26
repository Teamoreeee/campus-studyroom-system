import zipfile
name = '校园自习室预约系统_提交材料/test.txt'
with zipfile.ZipFile(r'C:\temp\test_chinese2.zip', 'w', zipfile.ZIP_DEFLATED) as zf:
    zf.writestr(name, 'hello')
with zipfile.ZipFile(r'C:\temp\test_chinese2.zip', 'r') as zf:
    info = zf.infolist()[0]
    print('flag_bits:', info.flag_bits)
    print('read name:', info.filename)
