using BasketballTickets.Domain;
using BasketballTickets.Service;
using log4net;

namespace BasketballTickets.Controller;

/// <summary>
/// Gestioneaza logica ecranului de Login.
/// Intr-o aplicatie WinForms/WPF ar fi conectat la fereastra de login.
/// Echivalent cu LoginController.java
/// </summary>
public class LoginController
{
    private static readonly ILog Logger = LogManager.GetLogger(typeof(LoginController));
    private readonly IService _service;

    public LoginController(IService service)
    {
        _service = service;
    }

    /// <summary>
    /// Apelat cand casierul apasa butonul "Login".
    /// Returneaza casierul autentificat sau null daca credentialele sunt gresite.
    /// </summary>
    public Cashier? HandleLogin(string username, string password)
    {
        Logger.Debug($"HandleLogin for username={username}");

        if (string.IsNullOrWhiteSpace(username) || string.IsNullOrWhiteSpace(password))
        {
            Logger.Warn("Login attempted with empty credentials");
            return null;
        }

        var cashier = _service.Login(username, password);

        if (cashier != null)
            Logger.Info($"Login successful for username={username}");
        else
            Logger.Warn($"Login failed for username={username}");

        // In WinForms/WPF: deschide fereastra CashierDashboard aici

        return cashier;
    }
}
