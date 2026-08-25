flag = "FLAG{0169145710bb4e301fed39201dd1a64c}"
key = 0x13

enc = [hex(ord(c) ^ key) for c in flag]
print(", ".join(enc) + ", 0x0")