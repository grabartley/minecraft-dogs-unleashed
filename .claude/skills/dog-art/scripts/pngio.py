import struct
import zlib

ALPHA_CUTOFF = 26
BACKGROUND = (58, 62, 70, 255)


def read_png(path):
    with open(path, "rb") as handle:
        data = handle.read()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise SystemExit(f"{path} is not a PNG")
    pos, chunks, palette, trns = 8, [], None, None
    width = height = depth = colour = interlace = None
    while pos < len(data):
        length, kind = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if kind == b"IHDR":
            width, height, depth, colour, _, _, interlace = struct.unpack(">IIBBBBB", body)
        elif kind == b"PLTE":
            palette = [tuple(body[i:i + 3]) for i in range(0, len(body), 3)]
        elif kind == b"tRNS":
            trns = body
        elif kind == b"IDAT":
            chunks.append(body)
        elif kind == b"IEND":
            break
    if depth != 8 or interlace:
        raise SystemExit(f"{path}: only 8-bit non-interlaced PNGs are supported")
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[colour]
    raw = zlib.decompress(b"".join(chunks))
    stride = width * channels
    rows, previous = [], bytearray(stride)
    for y in range(height):
        start = y * (stride + 1)
        line = unfilter(raw[start], bytearray(raw[start + 1:start + 1 + stride]), previous, channels)
        rows.append(to_rgba(line, channels, colour, palette, trns))
        previous = line
    return width, height, rows


def unfilter(filter_type, line, previous, channels):
    for i in range(len(line)):
        left = line[i - channels] if i >= channels else 0
        up = previous[i]
        up_left = previous[i - channels] if i >= channels else 0
        if filter_type == 1:
            line[i] = (line[i] + left) & 255
        elif filter_type == 2:
            line[i] = (line[i] + up) & 255
        elif filter_type == 3:
            line[i] = (line[i] + ((left + up) >> 1)) & 255
        elif filter_type == 4:
            line[i] = (line[i] + paeth(left, up, up_left)) & 255
    return line


def paeth(left, up, up_left):
    p = left + up - up_left
    pa, pb, pc = abs(p - left), abs(p - up), abs(p - up_left)
    if pa <= pb and pa <= pc:
        return left
    return up if pb <= pc else up_left


def to_rgba(line, channels, colour, palette, trns):
    out = []
    for i in range(0, len(line), channels):
        px = line[i:i + channels]
        if colour == 6:
            out.append((px[0], px[1], px[2], px[3]))
        elif colour == 2:
            out.append((px[0], px[1], px[2], 255))
        elif colour == 0:
            out.append((px[0], px[0], px[0], 255))
        elif colour == 4:
            out.append((px[0], px[0], px[0], px[1]))
        else:
            r, g, b = palette[px[0]]
            alpha = trns[px[0]] if trns and px[0] < len(trns) else 255
            out.append((r, g, b, alpha))
    return out


def write_png(path, width, height, rows):
    raw = bytearray()
    for row in rows:
        raw.append(0)
        for r, g, b, a in row:
            raw += bytes((r, g, b, a))
    with open(path, "wb") as handle:
        handle.write(b"\x89PNG\r\n\x1a\n")
        handle.write(chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)))
        handle.write(chunk(b"IDAT", zlib.compress(bytes(raw), 9)))
        handle.write(chunk(b"IEND", b""))


def chunk(kind, body):
    return struct.pack(">I", len(body)) + kind + body + struct.pack(">I", zlib.crc32(kind + body) & 0xFFFFFFFF)


def opaque(pixel):
    return pixel[3] >= ALPHA_CUTOFF


def blank(width, height, colour=BACKGROUND):
    return [[colour] * width for _ in range(height)]


def paste(target, source, x_off, y_off):
    for y, row in enumerate(source):
        for x, px in enumerate(row):
            target[y + y_off][x + x_off] = px
