namespace TodoApi.Dtos
{
    public class StartAuthResponse
    {
        public string Nonce { get; set; } = null!;
        public string LoginUrl { get; set; } = null!;
    }
}
