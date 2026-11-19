package extrator

import (
	"context"
	"errors"
	"fmt"
	"html"
	"io"
	"net/url"
	"regexp"
	"strings"
)

const limiteBytesPagina = 1 << 20 // 1 MiB

var (
	ErrURLInvalida   = errors.New("url invalida")
	ErrFalhaAoBuscar = errors.New("falha ao buscar a pagina")

	reTitulo   = regexp.MustCompile(`(?is)<title[^>]*>(.*?)</title>`)
	reLinkTag  = regexp.MustCompile(`(?is)<link\b[^>]*>`)
	reRelIcon  = regexp.MustCompile(`(?is)\brel\s*=\s*(?:["'][^"']*\bicon\b[^"']*["']|icon\b)`)
	reHrefAttr = regexp.MustCompile(`(?is)\bhref\s*=\s*["']([^"']+)["']`)
)

type Metadados struct {
	Titulo     string
	FaviconURL string
}

type Buscador interface {
	Buscar(ctx context.Context, rawURL string) (io.ReadCloser, error)
}

type Extrator struct{ buscador Buscador }

func NovoExtrator(buscador Buscador) (*Extrator, error) {
	if buscador == nil {
		return nil, errors.New("buscador obrigatorio")
	}
	return &Extrator{buscador: buscador}, nil
}

func (e *Extrator) Extrair(ctx context.Context, rawURL string) (Metadados, error) {
	if err := ctx.Err(); err != nil {
		return Metadados{}, fmt.Errorf("contexto cancelado: %w", err)
	}
	base, err := validarURL(rawURL)
	if err != nil {
		return Metadados{}, err
	}
	corpo, err := e.buscador.Buscar(ctx, base.String())
	if err != nil {
		if errors.Is(err, context.DeadlineExceeded) || errors.Is(err, context.Canceled) {
			return Metadados{}, fmt.Errorf("extrair %q: %w", rawURL, err)
		}
		return Metadados{}, fmt.Errorf("extrair %q: %w (%v)", rawURL, ErrFalhaAoBuscar, err)
	}
	defer corpo.Close()
	dados, err := io.ReadAll(io.LimitReader(corpo, limiteBytesPagina))
	if err != nil {
		return Metadados{}, fmt.Errorf("ler %q: %w (%v)", rawURL, ErrFalhaAoBuscar, err)
	}
	conteudo := string(dados)
	return Metadados{
		Titulo:     extrairTitulo(conteudo),
		FaviconURL: extrairFavicon(base, conteudo),
	}, nil
}

func validarURL(raw string) (*url.URL, error) {
	u, err := url.ParseRequestURI(strings.TrimSpace(raw))
	if err != nil || (u.Scheme != "http" && u.Scheme != "https") || u.Host == "" {
		return nil, fmt.Errorf("url %q: %w", raw, ErrURLInvalida)
	}
	return u, nil
}

func extrairTitulo(htmlTexto string) string {
	m := reTitulo.FindStringSubmatch(htmlTexto)
	if len(m) < 2 {
		return ""
	}
	return strings.TrimSpace(html.UnescapeString(m[1]))
}

func extrairFavicon(base *url.URL, htmlTexto string) string {
	for _, tag := range reLinkTag.FindAllString(htmlTexto, -1) {
		if !reRelIcon.MatchString(tag) {
			continue
		}
		if href := reHrefAttr.FindStringSubmatch(tag); len(href) >= 2 {
			if ref, err := url.Parse(strings.TrimSpace(href[1])); err == nil {
				return base.ResolveReference(ref).String()
			}
		}
	}
	return fmt.Sprintf("%s://%s/favicon.ico", base.Scheme, base.Host)
}
