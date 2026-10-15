# Recursive combination

`combination.as` เป็น assembly ต้นฉบับ และ `combination.mc` เป็น machine code ที่แปลงด้วย Assembler ของกลุ่ม
ไม่ได้เปลี่ยนโค้ด assembler หรือ simulator เพื่อให้โปรแกรมนี้ผ่าน

คำนวณด้วยสูตร:

```text
C(n, 0) = C(n, n) = 1
C(n, r) = C(n-1, r) + C(n-1, r-1)
```

รับค่าจาก label `n` และ `r` โดยใช้ input ที่ `0 <= r <= n`
ค่าเริ่มต้นคือ n = 7, r = 3 ผลลัพธ์เมื่อ halt อยู่ใน register 3 เป็น 35
หากเปลี่ยน `.fill` ของ n หรือ r ต้องแปลง assembly เป็น machine code ใหม่ก่อนรัน

| Register | หน้าที่ |
|---|---|
| 0 | ค่าศูนย์ |
| 1 | n ของการเรียกปัจจุบัน |
| 2 | r ของการเรียกปัจจุบัน |
| 3 | ผลลัพธ์ที่คืนจากฟังก์ชัน |
| 4 | ที่อยู่ฟังก์ชัน / ผลลัพธ์แรก / ค่าชั่วคราวตอน return |
| 5 | stack pointer ชี้ช่องว่างถัดไป นับจาก label stack |
| 6 | ค่าชั่วคราว +1 หรือ -1 |
| 7 | ที่อยู่สำหรับ return |

ก่อนเรียกซ้ำ เก็บ return address, n และ r ลง stack
หลังเรียกครั้งแรก เก็บผลลัพธ์ลง stack แล้วเรียกครั้งที่สอง
เมื่อนำผลลัพธ์มาบวกกันแล้ว กู้ r, n และ return address ก่อน return
แต่ละการเรียกจึงใช้ข้อมูลของตัวเองโดยไม่ทับของผู้เรียก
label `stack` อยู่ท้ายไฟล์เพื่อให้ stack ขยายไปยัง address ที่สูงขึ้น

## รันใน IntelliJ

- Main class: `Simulator`
- Working directory: โฟลเดอร์ `PC+INSRUCTIONMEM` ตามการตั้งค่าเดิม
- Program arguments: `../Assembly_Programs/combination.mc`

หาก working directory เป็น root โปรเจกต์ ใช้ `Assembly_Programs/combination.mc` แทน
ดู final state: register 3 = 35, register 5 = 0, register 1 = 7, register 2 = 3
โปรแกรมมี 44 words และกรณีเริ่มต้นรัน 1,179 instructions รวม halt

## ผลทดสอบ

ทดสอบผ่าน Assembler และ Simulator ปัจจุบันของกลุ่ม
ตรวจผลลัพธ์, stack pointer คืนเป็น 0 และ n/r คืนค่าเดิมทุกกรณี

| n | r | ผลลัพธ์ | Instructions |
|---:|---:|---:|---:|
| 7 | 3 | 35 | 1179 |
| 0 | 0 | 1 | 8 |
| 1 | 0 | 1 | 8 |
| 1 | 1 | 1 | 9 |
| 5 | 2 | 10 | 318 |
| 5 | 3 | 10 | 320 |
| 7 | 1 | 7 | 213 |
| 7 | 6 | 7 | 218 |
| 9 | 4 | 126 | 4314 |
