# Computer Architecture Project — ส่วนของ Pupa

หน้าที่: PC + Instruction Memory ของ behavioral simulator ใช้ `int` ขนาด 32 บิต
และ word address โดย PC เพิ่มทีละ 1 ไม่ใช่ทีละ byte
รับผิดชอบเชื่อม Register ของ Din เข้ากับวงรอบ instruction ด้วย

## สิ่งที่เตรียมไว้

| ไฟล์ | ทำอะไร | ใช้เชื่อมกับใคร |
|---|---|---|
| `MachineCodeLoader.java` | อ่านเลขฐานสิบหนึ่ง word ต่อบรรทัด ตรวจไฟล์และจำนวนข้อมูล | รับ machine-code file จาก TeeTad |
| `BitConverter.java` | แปลง `int` กับ `boolean[32]` รวมค่าลบ | RegisterBridge ส่ง fromBits32 ให้ Reg ของ Din ใช้ตอนอ่าน |
| `RegisterBridge.java` | อ่าน/เขียน register ของ Din ก่อนและหลังรัน instruction | เชื่อม Reg กับ state ที่ ControlUnit และ printState ใช้ |
| `InstructionDecoder.java` | แยก opcode, regA, regB, destReg และ offset | เตรียมส่ง field ให้ส่วน immediate และ execute ของ TJ/Zeline |
| `Simulator.java` | แสดง state, เรียก ControlUnit และนับคำสั่ง | loop กลาง ใช้ StateType ร่วมกับ ControlUnit |
| `ImmGenAndControlUnit/ControlUnit.java`, `ImmGen.java` | ใช้ execute ของเพื่อนจาก main | ปรับเฉพาะ package, จุดเรียก และการส่ง error ให้ Simulator |
| `SimulatorIntegrationTests.java` | ตรวจ countdown, output, memory และ jalr | ทดสอบการเชื่อมกับงานเพื่อน |

## รันจาก PowerShell ที่ root โปรเจ็กต์

ต้องมี JDK 17 ขึ้นไป

```powershell
$sourceDirs = @('PC+INSRUCTIONMEM', 'ImmGenAndControlUnit', 'ALU_ControlALU/src', 'MachineCode_Converter', 'RegAndMem')
$sources = $sourceDirs | ForEach-Object { Get-ChildItem -LiteralPath $_ -Filter '*.java' }
javac -encoding UTF-8 -d '.\out' $sources.FullName
java -cp '.\out' Simulator '.\MachineCode_Converter\machine-code-file'
java -cp '.\out' Simulator '.\PC+INSRUCTIONMEM\machine_code.txt'
java -cp '.\out' Simulator '.\PC+INSRUCTIONMEM\examples\countdown.txt'
java -cp '.\out' SimulatorIntegrationTests '.\PC+INSRUCTIONMEM\examples\countdown.txt'
```

ถ้ารับไฟล์จาก TeeTad ให้เปลี่ยน argument สุดท้ายเป็น path ของไฟล์นั้น
ไฟล์ input ต้องมีเลขฐานสิบแบบ signed 32-bit หนึ่งค่าต่อบรรทัด ไม่มี comment หรือบรรทัดว่าง
โหลดทั้งคำสั่งและข้อมูล `.fill` ลงใน `state.mem` ชุดเดียว และ `numMemory` เท่ากับจำนวนบรรทัด

ถ้าไม่ส่ง argument โปรแกรมจะหา `machine_code.txt` ใน working directory
ใน IntelliJ ใช้ Main class `Simulator` และเลือกอย่างใดอย่างหนึ่ง:

- ตั้ง Program arguments เป็น path เต็มของ machine-code file
- ตั้ง Working directory เป็นโฟลเดอร์ `PC+INSRUCTIONMEM` แล้วไม่ใส่ argument

ไฟล์ตัวอย่างที่มีอยู่เป็น `noop` ตามด้วย `halt` ควรเห็น PC `0 → 1 → 2`
นับ 2 instructions รวม halt และแสดง state ทั้งหมด 3 ครั้ง

## วิธีเชื่อมงานเพื่อน

1. **TeeTad:** ส่งออก signed decimal หนึ่งค่าต่อบรรทัด แล้วส่ง path นั้นให้ Simulator
   ตัวอย่างจาก origin/Teetad รุ่น ea3a275 อยู่ใน `PC+INSRUCTIONMEM/examples/countdown.as`
   และ `countdown.txt` ควรได้ 17 instructions, 18 states, PC สุดท้าย 7, reg1 = 0 และ reg2 = -1
   ใน IntelliJ ตั้ง Program arguments เป็น `examples/countdown.txt` เมื่อใช้ working directory เดิม
2. **Din:** RegisterBridge สร้าง Reg โดยส่ง `BitConverter::fromBits32` ให้ constructor ใหม่
   Reg จึงใช้ตัวแปลงของ Pupa ก่อนคืน int ทำให้ค่าลบรวมถึง Integer.MIN_VALUE อ่านถูก
   ก่อน instruction อ่าน Reg เข้าสู่ state.reg; หลัง instruction เขียนกลับ Reg แล้วอ่านกลับเพื่อแสดง state
   state.reg เป็นสำเนาสำหรับ interface ของอาจารย์ ไม่ใช่ register อีกชุดที่เปลี่ยนแยกกัน
   การสร้าง `new Reg()` แบบเดิมยังใช้ตัวแปลงเดิมของ Din เพื่อไม่เปลี่ยนพฤติกรรม module อื่น
3. **TJ/Zeline:** Simulator เรียก `ControlUnit.controlUnit(state)` และให้เพื่อนดูแล execute/ALU
   Simulator, ControlUnit และ ImmGen ใช้ default package เพื่อเชื่อม StateType เดิม
   เก็บไฟล์ไว้ในโฟลเดอร์ของแต่ละส่วน ไม่สร้างสำเนาชื่อคลาสซ้ำใน PC+INSRUCTIONMEM
   ControlUnit เป็นผู้เปลี่ยน PC; Simulator ไม่เพิ่ม PC ซ้ำ

## ส่วนที่ยังต้องตกลงร่วมกัน

- เชื่อมเฉพาะ Reg ของ Din แล้ว ยังไม่เชื่อม DataMem; memory ใช้ state.mem ชุดเดียวตามเดิม
- งาน DataMem เช่น signed conversion, byte/word address และขอบเขต เป็นงานร่วมกับ Din/Zeline
  รอบนี้ไม่แก้ DataMem, Latch หรือ OneByteMem
- execute ครบ 8 คำสั่งแล้ว; ทดสอบ countdown และคำสั่งที่เหลือด้วย integration checks
  โปรแกรมคูณและ recursive ของกลุ่มยังต้องนำมาทดสอบเพิ่มเติม
- `printState` และโครง `StateType` ใช้รูปแบบเดิมของโจทย์

## ข้อความสั้น ๆ สำหรับประชุม

> เราทำ PC, ตัวโหลด machine code, instruction memory และการแสดง state
> เชื่อม Register ของ Din โดยใช้ตัวแปลงบิตของเรา รองรับค่าลบครบช่วง int
> ก่อนรันคำสั่งอ่าน register ของ Din และหลังรันเขียนผลกลับ โดยใช้ execute ของ TJ
> ไม่ได้เขียน assembler, ALU หรือ Data Memory แทนเพื่อน

## Pseudocode ส่วน PC + Instruction Memory สำหรับรายงาน

```text
state = initial PC 0, memory 65536 words, registers 8 values
load signed machine-code words into state.mem; record numMemory
registers = RegisterBridge(state), using Din Reg with signed-bit converter
repeat:
    check PC is in range
    read Din registers into state.reg
    printState(state)
    halted = ControlUnit(state)  // fetch state.mem[PC], execute, update PC
    write state.reg back to Din registers
    read registers back into state.reg
    instructionCount++
    if halted: stop
print instructionCount and final printState(state)
```

เนื้อหา AsmReader, readAndParse, ErrorChecker และ ConvertMachineCode เป็นหัวข้อ assembler ของ TeeTad
ควรแยกออกจากหัวข้อ PC + Instruction Memory ในรายงาน
