//
// Terminal script for testing the superpeer overlay
// @author: Kevin Beineke, kevin.beineke@hhu.de, 21.12.2016
//
dxterm.cmd("nodelist").exec()
dxterm.cmd("metadatasummary").exec("all")
dxterm.cmd("chunkcreate").exec(10,0x280)
dxterm.cmd("namereg").exec(0x280,1,"a")
dxterm.cmd("chunkcreate").exec(10,0x280)
dxterm.cmd("namereg").exec(0x280,2,"b")
dxterm.cmd("chunkcreate").exec(10,0x280)
dxterm.cmd("namereg").exec(0x280,3,"c")
dxterm.cmd("chunkcreate").exec(10,0xC241)
dxterm.cmd("namereg").exec(0x280,1,"d")
dxterm.cmd("chunkcreate").exec(10,0xC241)
dxterm.cmd("namereg").exec(0x280,2,"e")
dxterm.cmd("chunkcreate").exec(10,0xC241)
dxterm.cmd("namereg").exec(0x280,3,"f")
dxterm.cmd("chunkcreate").exec(10,0xC241)
dxterm.cmd("namereg").exec(0x280,4,"g")
dxterm.cmd("chunkcreate").exec(10,0xC241)
dxterm.cmd("namereg").exec(0x280,5,"h")
dxterm.cmd("chunkcreate").exec(10,0xC601)
dxterm.cmd("namereg").exec(0x280,1,"i")
dxterm.cmd("chunkcreate").exec(10,0xC601)
dxterm.cmd("namereg").exec(0x280,2,"j")
dxram.sleep(3000)
dxterm.cmd("metadatasummary").exec("all")
dxterm.cmd("tmpcreate").exec(1,100)
dxterm.cmd("tmpcreate").exec(2,200)
dxterm.cmd("tmpcreate").exec(3,50)
dxterm.cmd("tmpcreate").exec(4,300)
dxterm.cmd("tmpcreate").exec(5,150)
dxram.sleep(3000)
dxterm.cmd("metadatasummary").exec("all")
dxterm.cmd("chunkmigrate").exec(0x280,1,0xC601)
dxterm.cmd("chunkmigrate").exec(0xC241,4,0x0280)
dxram.sleep(3000)
dxterm.cmd("metadatasummary").exec("all")
dxterm.cmd("nodeshutdown").exec(0xC301,true)
dxterm.cmd("nodeshutdown").exec(0xC0C1,true)
dxram.sleep(3000)
dxterm.cmd("metadatasummary").exec("all")
exit()