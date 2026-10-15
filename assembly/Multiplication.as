        lw      0       2       mcand   $2 = mcand (shift left ทุกรอบ)
        lw      0       3       mplier  $3 = mplier
        lw      0       4       pos1    $4 = mask = 1
        lw      0       6       lim     $6 = 32768 (ค่าที่ใช้เช็คว่า mask ครบ 15 bits)
loop    nand    3       4       5       $5 = NAND(mplier, mask)
        nand    5       5       5       $5 = AND(mplier, mask)
        beq     5       0       1       ถ้า AND == 0 (bit เป็น 0) ข้าม add
        add     1       2       1       product ($1) += mcand
        add     2       2       2       mcand = mcand * 2 (shift left)
        add     4       4       4       mask = mask * 2 (shift left)
        beq     4       6       1       ถ้า mask == 32768 กระโดดไป halt
        beq     0       0       loop    ยังไม่ครบ วนกลับ
        halt
mcand   .fill   32766
mplier  .fill   10383
pos1    .fill   1
lim     .fill   32768