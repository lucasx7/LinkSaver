package grpcserver

import (
	"context"
	"errors"

	metadadosv1 "github.com/lucasx7/LinkSaver/services/extrator-go/gen/linksaver/metadados/v1"
	"github.com/lucasx7/LinkSaver/services/extrator-go/internal/extrator"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/health"
	healthpb "google.golang.org/grpc/health/grpc_health_v1"
	"google.golang.org/grpc/reflection"
	"google.golang.org/grpc/status"
)

type Servidor struct {
	metadadosv1.UnimplementedMetadadosServiceServer
	ext *extrator.Extrator
}

func RegistrarServicos(srv *grpc.Server, ext *extrator.Extrator) {
	metadadosv1.RegisterMetadadosServiceServer(srv, &Servidor{ext: ext})
	hs := health.NewServer()
	hs.SetServingStatus("", healthpb.HealthCheckResponse_SERVING)
	hs.SetServingStatus("linksaver.metadados.v1.MetadadosService", healthpb.HealthCheckResponse_SERVING)
	healthpb.RegisterHealthServer(srv, hs)
	reflection.Register(srv)
}

func (s *Servidor) ExtrairMetadados(
	ctx context.Context,
	req *metadadosv1.ExtrairMetadadosRequest,
) (*metadadosv1.ExtrairMetadadosResponse, error) {
	meta, err := s.ext.Extrair(ctx, req.GetUrl())
	if err != nil {
		switch {
		case errors.Is(err, extrator.ErrURLInvalida):
			return nil, status.Error(codes.InvalidArgument, err.Error())
		case errors.Is(err, context.DeadlineExceeded):
			return nil, status.Error(codes.DeadlineExceeded, err.Error())
		case errors.Is(err, context.Canceled):
			return nil, status.Error(codes.Canceled, err.Error())
		default:
			return nil, status.Error(codes.Unavailable, err.Error())
		}
	}
	return &metadadosv1.ExtrairMetadadosResponse{
		Titulo:     meta.Titulo,
		FaviconUrl: meta.FaviconURL,
	}, nil
}
