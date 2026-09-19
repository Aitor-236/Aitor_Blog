import { marked, Renderer } from 'marked'
import DOMPurify from 'dompurify'

/**
 * 正文和预览共用一份渲染规则：marked 解析 + DOMPurify 清洗。
 * 详情页和编辑页都调 renderMarkdown，避免两处规则漂移。
 */

/** marked 的默认代码块渲染，转义规则仍交给它处理，我们只在外面套一层语言标签。 */
const defaultRenderer = new Renderer()
const renderDefaultCode = defaultRenderer.code.bind(defaultRenderer)

function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

marked.use({
  renderer: {
    /**
     * 代码围栏上写了语言（```java）时，额外包一层容器并在左上角显示语言名；
     * 没写语言的代码块保持原样，不加空标签。
     */
    code(token) {
      const html = renderDefaultCode(token)
      // 围栏后面可能还有别的信息（```java title="x"），只取第一个词
      const language = (token.lang ?? '').trim().split(/\s+/)[0] ?? ''
      if (!language) return html
      return `<div class="code-block"><span class="code-block-lang">${escapeHtml(language)}</span>${html}</div>\n`
    }
  }
})

/** 清洗后的 HTML 里，src 分隔符一定是双引号，这里只补前缀、不碰其它地址。 */
const UPLOAD_IMAGE_SRC = /(<img\b[^>]*?\ssrc=")\/uploads\//gi

/** Markdown 原文 → 可直接 v-html 的 HTML。 */
export function renderMarkdown(source: string): string {
  const html = marked.parse(source, { async: false }) as string
  // 上传的图片在库里存的是 /uploads/... 相对地址（等于接口路径去掉 /api 前缀），
  // 渲染时统一补上 /api：开发由 vite、生产由 nginx 去掉前缀转发，
  // 这样正文里手写的 ![](/uploads/article/x.png) 也能正常显示。
  return DOMPurify.sanitize(html).replace(UPLOAD_IMAGE_SRC, '$1/api/uploads/')
}
