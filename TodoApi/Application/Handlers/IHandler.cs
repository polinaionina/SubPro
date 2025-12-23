namespace TodoApi.Application.Handlers;

public interface IHandler<TIn, TOut>
{
    Task<TOut> Handle(TIn input);
}
