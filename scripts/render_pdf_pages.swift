import AppKit
import Foundation
import PDFKit

guard CommandLine.arguments.count == 3 else {
    fputs("Uso: render_pdf_pages.swift <archivo.pdf> <directorio-salida>\n", stderr)
    exit(2)
}

let inputURL = URL(fileURLWithPath: CommandLine.arguments[1])
let outputURL = URL(fileURLWithPath: CommandLine.arguments[2], isDirectory: true)
let fileManager = FileManager.default
try fileManager.createDirectory(at: outputURL, withIntermediateDirectories: true)

guard let document = PDFDocument(url: inputURL) else {
    fputs("No fue posible abrir el PDF.\n", stderr)
    exit(1)
}

let scale: CGFloat = 2.0

for pageIndex in 0..<document.pageCount {
    guard let page = document.page(at: pageIndex) else { continue }
    let bounds = page.bounds(for: .mediaBox)
    let width = Int(ceil(bounds.width * scale))
    let height = Int(ceil(bounds.height * scale))

    guard let bitmap = NSBitmapImageRep(
        bitmapDataPlanes: nil,
        pixelsWide: width,
        pixelsHigh: height,
        bitsPerSample: 8,
        samplesPerPixel: 4,
        hasAlpha: true,
        isPlanar: false,
        colorSpaceName: .deviceRGB,
        bytesPerRow: 0,
        bitsPerPixel: 0
    ) else { continue }

    NSGraphicsContext.saveGraphicsState()
    guard let context = NSGraphicsContext(bitmapImageRep: bitmap) else {
        NSGraphicsContext.restoreGraphicsState()
        continue
    }
    NSGraphicsContext.current = context
    NSColor.white.setFill()
    NSRect(x: 0, y: 0, width: width, height: height).fill()
    context.cgContext.scaleBy(x: scale, y: scale)
    page.draw(with: .mediaBox, to: context.cgContext)
    context.flushGraphics()
    NSGraphicsContext.restoreGraphicsState()

    guard let png = bitmap.representation(using: .png, properties: [:]) else { continue }
    let filename = String(format: "page-%03d.png", pageIndex + 1)
    try png.write(to: outputURL.appendingPathComponent(filename))
}

print("Renderizadas \(document.pageCount) páginas en \(outputURL.path)")
